package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 要调用coupon服务中的save方法，就要声明上发出的请求url
     * 调用逻辑
     *  1。 某个服务内调用了CouponFeignService.saveSpuBounds(spuBoundTo)
     *  2。@RequestBody 把这个spuBoundsTo对象转为json
     *  3。 找到gulimall-coupon服务，并向/coupon/spubounds/save发送请求，将上一步转的json放在请求体，发送请求
     *  4。对方服务收到请求，请求题里有json数据，并且对方@RequestBody将请求体里的json转为对象
     *
     *  知识点：只要发送的对象和接收的对象内封装的数据格式相同，及时不是双方不是不是同一类型的对象，也可以完成json相格式相互转换
     *  spuBoundTo里的三个属性在SpuBoundsEntity里都有，所以接收方的方法参数无需更换成和这里的一样
     *
     * @param spuBoundsTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
