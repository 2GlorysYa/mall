package com.atguigu.gulimall.authserver.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.vo.SocialUser;
import com.atguigu.gulimall.authserver.vo.UserLoginVo;
import com.atguigu.gulimall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    // userRegisterVo对应gulimall-member里的MemberRegisterVo
    @PostMapping("/member/member/register") // member
    public R register(@RequestBody UserRegistVo Vo);

    // userLoginVo对应gulimall-member里的MemberLoginVo
    @PostMapping("/member/member/login") // member
    public R login(@RequestBody UserLoginVo loginVo);

    @PostMapping("/member/member/oauth2/login") // member
    public R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
