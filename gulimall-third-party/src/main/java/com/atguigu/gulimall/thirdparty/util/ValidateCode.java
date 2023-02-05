package com.atguigu.gulimall.thirdparty.util;

import org.springframework.stereotype.Component;

import java.util.Random;

// 生成随机验证码
@Component
public class ValidateCode {

    // 传入验证码长度，4位或6位
    public static Integer generateValidateCode(int length) {
        Integer code = null;
        if (length == 4) {
            code = new Random().nextInt(9999);
            if (code < 1000) {
                code = code + 1000;
            }
        } else if (length == 6){
            code = new Random().nextInt(999999);
            if (code < 100000) {
                code = code + 100000;
            }
        } else {
            throw new IllegalArgumentException("只能生成4位或6位数字验证码");
        }
        return code;
    }
}
