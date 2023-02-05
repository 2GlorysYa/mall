package com.atguigu.gulimall.authserver.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;


// https://gitee.com/oauth/token?grant_type=authorization_code&code=9bb9d110c761383e27c9d1f880f5f86f066aec92bde60a3b450d6519fc0192fa&client_id=8afeb1a002af5ddd607f287a1025a423a9d81fc4c1509a580f100abcded1486b&redirect_uri=http://gulimall.com/success&client_secret=9cf4ef40e264ad3bf4faf8d4aa68573c5692d736778bbca9c129c7292ce4c9ee
// 根据Post请求返回的token信息封装到Vo实体类里
//{
//     "access_token": "d4e5e1407411bf781f0aeb2b00d7dcb4",
//     "token_type": "bearer",
//     "expires_in": 86400,
//     "refresh_token": "1a9f53d37300096daf10c8a01ef7eb69360f598c42a2afff7d701a93c247b0d1",
//     "scope": "user_info",
//     "created_at": 1645331058
// }
@Data
public class SocialUser {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;
    private String scope;
    private long createdAt;

    @TableField(value = "social_uid")
    private String id; // gitee实际保存的是id这里映射到数据库的socialUid

}
