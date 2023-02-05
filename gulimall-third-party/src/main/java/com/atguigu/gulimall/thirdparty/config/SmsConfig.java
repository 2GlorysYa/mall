package com.atguigu.gulimall.thirdparty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "tencent.sms")
@Component
@Data
public class SmsConfig {

    /**
     * 腾讯云API密钥的SecretId
     */
    private String secretId;
    /**
     * 腾讯云API密钥的SecretKey
     */
    private String secretKey;
    /**
     * 短信应用的SDKAppID
     */
    private String appId;
    /**
     * 签名内容
     */
    private String sign;
    /**
     * 模板ID
     */
    private String templateId;
    /**
     * 过期时间
     */
    private String expireTime;
}
