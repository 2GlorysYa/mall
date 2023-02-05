package com.atguigu.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.authserver.feign.MemberFeignService;
import com.atguigu.gulimall.authserver.vo.MemberResponseVo;
import com.atguigu.gulimall.authserver.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    // http://auth.gulimall.com/oauth2.0/gitee/success
    @GetMapping("/oauth2.0/gitee/success")
    public String authorize(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 1. 使用code换取token，换取成功则继续2，否则重定向至登录页
        // http://gulimall.com/oauth2.0/gitee/success&code=xxxxxx
        // 所携带的请求参数
        Map<String, String> query = new HashMap<>();
        query.put("client_id", "8afeb1a002af5ddd607f287a1025a423a9d81fc4c1509a580f100abcded1486b");
        query.put("client_secret", "9cf4ef40e264ad3bf4faf8d4aa68573c5692d736778bbca9c129c7292ce4c9ee");
        query.put("grant_type", "authorization_code");
        query.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        query.put("code", code);

        // 发送post请求换取token，响应体包含uid
        // https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<String, String>(), query, new HashMap<String, String>());
        Map<String, String> errors = new HashMap<>();
        // 如果状态码为200，则获取成功
        if (response.getStatusLine().getStatusCode() == 200) {
            //2. 调用member远程接口进行oauth登录，登录成功则转发至首页并携带返回用户信息，否则转发至登录页
            // 调用Apache的EntityUtils工具类，获得【响应体】内容 (JSON字符串) 并转换为字符串
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, new TypeReference<SocialUser>() {});

            // 知道当前是哪个社交用户
            // 1。当前用户如果是第一次进入网站，应该自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应着这个用户）
            // 否则就进行登录
            // 拿着accessToken查询用户信息
            if (socialUser != null && (!StringUtils.isEmpty(socialUser.getAccessToken()))) {

                Map<String, String> queryAccessToken = new HashMap<>();
                queryAccessToken.put("access_token", socialUser.getAccessToken());

                Map<String, String> queryHeaders = new HashMap<>();
                queryHeaders.put("Content-Type", "application/json;charset=UTF-8");

                HttpResponse response1 = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", queryHeaders, queryAccessToken);
                if (response1.getStatusLine().getStatusCode() == 200) {
                    String json1 = EntityUtils.toString(response1.getEntity());

                    // 获取user_info
                    SocialUser socialUser1 = JSON.parseObject(json1, new TypeReference<SocialUser>() {
                    });
                    socialUser1.setAccessToken(socialUser.getAccessToken());
                    socialUser1.setExpiresIn(socialUser.getExpiresIn());
                    // socialUser1.setId(socialUser.getId());

                    // TODO 社交账号登录和注册为一体
                    // 注意调试时远程调用容易超时异常
                    R oauthlogin = memberFeignService.oauthlogin(socialUser1);
                    //2.1 远程调用成功，返回首页并携带用户信息
                    if (oauthlogin.getCode() == 0) {
                        MemberResponseVo data = oauthlogin.getData("data", new TypeReference<MemberResponseVo>(){});
                        log.info("登录成功： 用户： {}", data.toString());
                        // 第一次使用session；命令浏览器保存卡号（sessionId）
                        // 以后浏览器访问哪个网站就会带上这个网站的cookie
                        // 子域名之间：gulimall.com auth.gulimall.com order.gulimall.com
                        // 发卡时（指定域名为父域名），即时是子域名系统下发的卡，也能让父域名直接使用
                        // 要想把memberResponseVo存储在redis，就必须把他序列化，否则会报不能序列化异常
                        // 默认会使用JDK序列化机制，因此memberResponseVo必须implements Serializable
                        // 原理是通过springSessionRepositoryFilter将原生的HttpSession替换成springSession
                        // TODO 1 扩大SESSION作用域
                        // TODO 2 使用JSON的序列化方式来序列化对象数据到redis
                        session.setAttribute(AuthServerConstant.LOGIN_USER, data);
                        // 登录成功，重定向到首页
                        return "redirect:http://gulimall.com/";
                    } else {
                        //2.2 否则返回登录页
                        errors.put("msg", "登录失败，请重试");
                        session.setAttribute("errors", errors);
                        // 登录失败，重定向到登录页面
                        return "redirect:http://auth.gulimall.com/login.html";
                    }
                } else {
                    errors.put("msg", "获得第三方授权失败，请重试");
                    session.setAttribute("errors", errors);
                    // 登录失败，重定向到登录页面
                    return "redirect:http://auth.gulimall.com/login.html";
                }
            }
        }
        errors.put("msg", "获得第三方授权失败，请重试");
        session.setAttribute("errors", errors);
        return "redirect:http://auth.gulishop.cn/login.html";
    }
}
