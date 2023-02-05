package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo =  LoginUserInterceptor.loginUser.get();

        // 为了避免feign异步下丢失上下文，先在主线程里获取到request，再共享给其他两个线程
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 异步任务
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 1. 远程查询所有的收货地址列表
            // 手动把主线程的request共享给子线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setAddress(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2. 远程查询购物车所有选中的购物项
            // 手动把主线程的request共享给子线程
            List<OrderItemVo> items = cartFeignService.getCurrentCartItems();
            confirmVo.setItems(items);
        }, threadPoolExecutor).thenRunAsync(() -> {
            // 再then收集商品的库存信息
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R hasStocks = wmsFeignService.getSkuHasStocks(collect);
            List<SkuStockVo> data = hasStocks.getData(new TypeReference<List<SkuStockVo>>() {});
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }

        }, threadPoolExecutor);


        // 3. 查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4. 其他数据自动计算

        // TODO 5. 防重复令牌
        // 每个订单【结算页】创建时会创建一个token，并封装成对象返回页面
        String token = UUID.randomUUID().toString().replace("-", "");
        // order:token:{userId}", "uuid
        // 令牌30分钟内有效
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);

        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddressFuture, cartFuture).get();


        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        // 把页面传过来的数据都封装到threadlocal方便向后面传递
        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        // 1.验证令牌 【关键在于令牌的对比和删除必须保证原子性】
        // 如果两次点击从页面获得得令牌都是123（getOrderToken()），在后台与redis里的123对比都成功
        // 所以要保证对比后删除是一个原子操作，这样第二次对比123就会失败
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        // lua脚本，如果redis get这个value等于传过去的值，就在redis删除这个key，否则返回0
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String redisToken = vo.getOrderToken();

        // 0令牌失败 1删除成功
        // 第一个参数RedisScript的实现类DefaultRedisScript，<T>是返回值类型，这里返回0或1
        // 第二个参数是KEYS[1]，也是一个数组，取第一个元素，传要验证的redis key
        // 第三个参数是ARGV[1]可变数组，就是要对比的页面传过来的值
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
                redisToken);
        if (result == 0L) {
            // 令牌验证失败,返回给controller，并设置状态码，controller根据状态码判断订单是否提交成功
            responseVo.setCode(1);
            return responseVo;
        } else {
            // 验证成功
            // 下单：去创建订单，验证令牌，验证价格，锁库存
            OrderCreateTo order = createOrder();

        }


        // 要保证原子性，应使用lua
        // redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
        // if (redisToken != null && redisToken.equals(redisToken)) {
        //     // 验证通过
        //     redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
        // } else {
        //     // 不通过
        // }

        return responseVo;
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1.生成订单号
        // 时间ID = Time + ID
        String orderSn = IdWorker.getTimeId();

        // 创建订单号
        OrderEntity orderEntity = buildOrder(orderSn);

        // 2.获取到所有的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems();

        // 3.验价


        return orderCreateTo;
    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // 获取收货地址信息
        R fare = wmsFeignService.getFare(orderSubmitVo.getAttrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>(){});

        orderEntity.setFreightAmount(fareResp.getFare());
        // 设置收货人信息
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        // 后面set的太多了就没写了.....
        
        return orderEntity;
    }

    /** 
     * 构建所有订单项数据，避免直接new一个对象
     * @return
     */
    private List<OrderItemEntity> buildOrderItems() {
        List<OrderItemVo> currentCartItems = cartFeignService.getCurrentCartItems();
        if (currentCartItems != null && currentCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentCartItems.stream().map(carItems -> {
                OrderItemEntity orderItemEntity = buildOrderItem(carItems);;

                return orderItemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 构建每一个订单项
     * @param carItems
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo carItems) {
        return null;
    }

}