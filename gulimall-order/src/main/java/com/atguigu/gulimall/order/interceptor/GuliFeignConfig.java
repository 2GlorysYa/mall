package com.atguigu.gulimall.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Yibo Lei
 */
@Configuration
public class GuliFeignConfig  {

    /**
     * 因为feign在构造时会判断容器里如果有拦截器，就把他add到interceptors里，所以只用返回一个拦截器给容器
     * 并在拦截器的执行方法apply里设置上新请求的cookie，这样就能把老请求头中的cookie带过去
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1. RequestContextHolder 是Spring 提供的一个用来暴露Request 对象的工具，利用RequestContextHolder，
                // 可以在一个请求线程中获取到Request，避免了Request 从头传到尾的情况
                // 它里面就是个ThreadLocal，存放requestAttribute
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        // 同步请求头数据，即把老请求头中的Cookie放到新请求requestTemplate里
                        String cookie = request.getHeader("Cookie");
                        requestTemplate.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
