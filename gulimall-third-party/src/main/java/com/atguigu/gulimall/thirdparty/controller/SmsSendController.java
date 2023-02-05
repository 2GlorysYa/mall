package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SendSmsCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    private SendSmsCode sendSmsCode;

    /*** 提供给别的服务进行调用的
     该controller是发给短信服务的，不是验证的
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, String code) {
        // if(!"fail".equals(sendSmsCode.send(phone).split("_")[0])){
        //     return R.ok();
        // }
        // return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
        sendSmsCode.send(phone, code);
        return R.ok();
    }
}
