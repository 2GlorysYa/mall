package com.atguigu.gulimall.authserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 属性绑定（声明上properties文件里的属性前缀）, 会自动读入properties中定义的三个属性，
@ConfigurationProperties(prefix="gulimall.thread")
@Component
@Data
public class ThreadPoolConfigproperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;

}
