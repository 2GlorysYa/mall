package com.atguigu.gulimall.thirdparty.component;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.config.SmsConfig;
import com.atguigu.gulimall.thirdparty.util.SmsUtil;
import com.atguigu.gulimall.thirdparty.util.ValidateCode;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendSmsCode {

    @Autowired
    SmsConfig smsConfig;

    public SendStatus[] send(String phoneNumber, String code) {
        // 手机好吗，国家/区号+手机号
        String[] phoneNumbers = {"+86" + phoneNumber};
        // 生成随机字符串
        // int rCode = ValidateCode.generateValidateCode(6);
        // String rc = String.valueOf(rCode);
        // 在验证服务里的loginController传入来的code包含系统时间，所以这里要拆开
        String[] templateParams = {code.split("_")[0]};
        // 发送短信验证码
        return SmsUtil.sendSms(smsConfig, templateParams, phoneNumbers);

        // if ("Ok".equals(sendStatuses[0].getCode())) {
        //     return R.ok();
        // } else {
        //     return R.error(sendStatuses[0].getMessage());
        // }
    }
}
