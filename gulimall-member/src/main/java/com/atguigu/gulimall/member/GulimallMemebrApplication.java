package com.atguigu.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.atguigu.gulimall.member.feign")    // 开启feign，注意声明feign包的全包名
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallMemebrApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallMemebrApplication.class, args);
    }
}
