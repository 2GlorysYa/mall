package com.atguigu.gulimall.cart.interceptor;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 执行目标方法之前，判断用户的登录状态，并封装传递给controller目标请求
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    // 封装临时用户的信息到UserInfoTo
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /***
     * 拦截所有请求并给ThreadLocal封装UserInfoTo对象
     * 1、从session中获取MemberResponseVo != null，登录状态，为UserInfoTo设置Id，表明是已登录用户
     * 2、从request中获取cookie，找到user-key的value，并同时set进临时用户的user-key
     * 所以UserInfoTo里可以同时包含已登录用户id和临时用户的标识
     * 不管是否登录，在postHandle里都会set user-key到cookie
     * 来保证下次用户访问时一定有身份（要么是临时用户，要么临时和登录一起）
     * 目标方法执行之前：在ThreadLocal中存入用户信息【同一个线程共享数据】
     * 从session中获取数据【使用session需要cookie中的GULISESSION 值】
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 使用一个传输对象
        UserInfoTo userInfoTo = new UserInfoTo();
        // 先获取session对象，再查看session里是否有内容
        HttpSession session = request.getSession();
        // 用session获得当前登录用户的信息
        // 在login方法里，最后执行session.setAttribute("loginUser", data);
        // 即当创建了一个session时，http响应就会携带一个指定的Cookie并且value是这个返回的会员对象
        // 因此下次访问浏览器会携带上这个cookie
        // 具体的set-cookie会通过Spring-session的Cookie序列化器转换为json字符串
        // 但如果是没有登录的情况下，也可以查看session，但get的对象会是空
        // 因此如果已登录，那么get的对象一定不为空
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (memberResponseVo != null) {
        // 如果用户已登录
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        // 不管用户登录与否，都查看下请求中的cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                // 如果cookie名为user-key，就把该user-key存入To里
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    // 标识客户端已经存储了 user-key
                    // 也即标识已经为客户端分配了一个临时用户，之后postHandler无需再分配
                    userInfoTo.setTempUser(true);
                }
            }
        }
        // 如果没有临时用户一定分配一个临时用户
        // 如果user-key是null，那就说明还未分配一个临时用户，一定要分配一个
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(userInfoTo);
        return true;
    }


    /**
     * postHandle在方法返回后执行
     * 不管用户是否已登录还是未登录，执行完毕之后分配临时用户让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();
        // 用户第一次访问时，是非临时用户，isTempUser = false
        // 如果是临时用户，需要把user-key作为临时用户的cookie返回，这样下次访问才会带上这个临时用户cookie
        // 如果已经分配了临时用户，就跳过不执行
        if(!userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            // 设置这个cookie作用域 过期时间
            cookie.setDomain("gulimall.com");   // 设置作用域，在整个gulimall都有效
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIME_OUT); // 设置过期时间1个月
            response.addCookie(cookie);
        }
    }

}
