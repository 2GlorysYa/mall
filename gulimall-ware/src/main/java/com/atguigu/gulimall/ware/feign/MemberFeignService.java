package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Yibo Lei
 */
@FeignClient("guliamll-member")
public interface MemberFeignService {

    @RequestMapping("member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
