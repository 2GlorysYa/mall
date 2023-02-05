package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 拦截器配置类
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    // 必须在配置累里添加拦截器，才能工作
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
