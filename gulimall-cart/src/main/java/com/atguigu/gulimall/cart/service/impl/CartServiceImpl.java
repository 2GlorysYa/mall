package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        // 根据登录状态拿到临时或登录购物车，getCartItemOps方法会从threadLocal里获取登录状态
        // 因此ops就相当于拿到了购物车，接下来对里 面的value，即一个map进行操作
        // key是商品skuId，value是商品详情，对应一个Object (k-v)
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        // 判断当前商品是否已经存在购物车， cartJson是一个json字符串，里面是{k:v}的map形式
        String cartJson = (String) ops.get(skuId.toString());
        // 1 如果是同一个商品的添加，那么skuId是已有的
        // 即商品已经存在购物车，就将数据取出并添加商品数量，+num
        if (!StringUtils.isEmpty(cartJson)) {
            // 注意carJson是存在redis里的json字符串，如果想要更新其中的count字段
            // 即商品数量+num，无法直接修改，必须将json字符串转为对象再修改
            //1.1 将json转为对象并将count+
            CartItem cartItemVo = JSON.parseObject(cartJson, CartItem.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);
            //1.2 将更新后的对象转为json并存入redis
            String jsonString = JSON.toJSONString(cartItemVo);
            ops.put(skuId.toString(), jsonString);
            return cartItemVo;
        } else {
            // 如果是不同商品的添加，即添加新商品到购物车，那么skuId也是新的
            // 商品新增
            CartItem cartItemVo = new CartItem();
            // 2 未存在购物车，则添加新商品
            // 使用异步编排先开启一个异步任务，并交给线程池执行
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                //2.1 远程查询sku基本信息
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData (new TypeReference<SkuInfoVo>() {
                });

                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setPrice(skuInfo.getPrice());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setTitle(skuInfo.getSkuTitle());
            }, executor);

            //2.2 远程查询sku属性组合信息
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                List<String> attrValuesAsString = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttr(attrValuesAsString);
            }, executor);

            try {
                // 执行两个异步任务, get()会等待这两个任务都完成再继续，否则阻塞在这里
                CompletableFuture.allOf(future1, future2).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //2.3 将该属性封装并存入redis,登录用户使用userId为key,否则使用user-key
            String toJSONString = JSON.toJSONString(cartItemVo);
            ops.put(skuId.toString(), toJSONString);
            return cartItemVo;
        }
    }

    /**
     * 页面添加新的购物项成功，后台就应该返回重新查询出来的该购物项信息
     * 获取购物车中某一个购物项
     * @param skuId 操作的购物项id
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> carOps = getCartOps();
        String str = (String) carOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);
        return cartItem;
    }

    /**
     * 点击我的购物车，显示当前购物车页面
     * 因此需要判断用户登录状态，并判断是否进行合并购物车操作
     * @return
     */
    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        // 首先通过threadLocal判断是否登录，因为你要先想清楚getCart获取哪个购物车
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        // 用户已登录，会考虑合并购物车
        if (!StringUtils.isEmpty(userInfoTo.getUserId())) {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            // 获取临时购物车的数据
            String tempCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            // 如果临时购物车有数据，则需要合并, 且只需要传入要添加的商品skuId和数量
            if (tempCartItems != null) {
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(),item.getCount());
                }
                // 清空临时购物车
                clearCart(tempCartKey);
            }
            // 再获取登录后的购物车数据【包含合并过来的临时购物车数据】
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        } else {
            // 用户没登录, 拿user-key
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            // 获取临时购物车所有的购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        // 直接删除redis中的key即可
        redisTemplate.delete(cartKey);
    }

    /**
     * 改变商品选中的状态
     * @param skuId
     * @param check 1为选中，0为未选中
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        // 先获取购物车中的这个购物项
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        // 修改好后还需要存回redis
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    /**
     * 改变商品的数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        // 先从购物车获取该商品
        CartItem cartItem = getCartItem(skuId);
        // 设置新数量
        cartItem.setCount(num);
        // 放回购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem ));
    }

    /**
     * 删除购物车中的商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        // 拿到购物车
        BoundHashOperations<String, Object, Object> cartOps =  getCartOps();
        // 直接在购物车里删除
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId()==0) {
            return null;
        } else {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            // 获取所有选中的购物项
            List<CartItem> collect = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item -> {
                        // 更新为最新价格
                        R price = productFeignService.getPrice(item.getSkuId());
                        String data = (String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
    }


    /**
     * 获取要操作的购物车
     * 不使用redisTemplate.opsForHash(), 而是使用redisTemplate.boundHashOps(key)
     * 他可以为一个key绑定上hashmap的操作
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //1判断是否已经登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //1.1 登录使用userId操作redis
        if (!StringUtils.isEmpty(userInfoTo.getUserId())) {
            return redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserId());
        } else {
            //1.2 未登录使用user-key操作redis
            return redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
        }
    }


    /**
     * 抽取一个方法来获取购物车中所有购物项
     * @param cartKey 要么是userId（登录）， 要么是user-key（未登录）
     * @return
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        // 拿到临时购物车里的所有购物详情，返回结果强制是object，所以需要先转为String，再一一解析为cartItem对象
        List<Object> values = hashOps.values(); // 返回hashmap中的【values】，也就是购物项数据
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

}
