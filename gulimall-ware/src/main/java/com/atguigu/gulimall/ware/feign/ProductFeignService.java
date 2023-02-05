package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     * feign有两种写法
     * 第一种是直接给服务发送请求，url是服务方法对应的url路径
     *      @FeginClient("gulimall-product)
     *      @RequestMapping("/product/skuinfo/info/{skuId}")
     * 第二种是给网关发送请求
     *      @FeignClient("gulimall-gateway")
     * @RequestMapping("api/product/skuinfo/info/{skuId}")
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}

