package com.atguigu.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
// 告诉SpringBoot将本类中所有属性和配置文件中相关的配置进行绑定
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;

}
