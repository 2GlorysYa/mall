package com.atguigu.gulimall.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallCorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 注意是导入reactive包里的，不是cors里的,
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 1。配置跨域
        // 允许任何请求头
        corsConfiguration.addAllowedHeader("*");
        // 允许提交请求的方法，比如get post delete put options
        corsConfiguration.addAllowedMethod("*");
        // 表示接收任何域名的请求
        corsConfiguration.addAllowedOrigin("*");
        // 允许cookie跨域
        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
