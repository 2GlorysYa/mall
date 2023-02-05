package com.atguigu.gulimall.authserver.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class GulimallSessionConfig {

    // springsession使用cookieserializer来管理cookie
    // 这个是cookie序列化，用于set-cookie
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        // 可以从浏览器看到，session的默认存活时间是"session"，也即浏览器关闭后就失效
        // 设置域名为父域名，解决子域共享问题，使得session可以在各个子域名间共享
        // 任何带gulimall.com的域名都可以使用这个session
        cookieSerializer.setDomainName("gulimall.com");
        cookieSerializer.setCookieName("GULISESSION");

        return cookieSerializer;
    }

    // 不使用jdk默认序列化，而是使用redis的json序列化
    // 这个是session序列化，序列化到redis的
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {

        return new FastJsonRedisSerializer<>(Object.class);
    }

}
