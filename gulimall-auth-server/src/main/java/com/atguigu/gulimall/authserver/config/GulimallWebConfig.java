package com.atguigu.gulimall.authserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {


    /**
     * 视图映射:发送一个请求，直接跳转到一个页面
     * WebMvcConfigurer的addViewControllers方法可以无需定制controller逻辑而渲染一个页面
     * 这样就不用在controller做简单跳转而没有方法体了
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // 把url映射到视图
        // registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
