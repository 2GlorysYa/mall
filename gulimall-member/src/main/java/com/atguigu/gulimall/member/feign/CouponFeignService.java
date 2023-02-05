package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

// 声明这个接口是fegin客户端，目的是告诉spring cloud这个接口要调用远程服务
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    // feign里写要调用的远程服务【方法签名】,但注意url是整个地址，因为feign会向这个url发http请求！
    @RequestMapping("/coupon/coupon/member/list")   // 要发的请求
    public R membercoupons();
}
