package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Yibo Lei
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkuHasStocks(@RequestBody List<Long> SkuIds);

    @GetMapping("/ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);
}
