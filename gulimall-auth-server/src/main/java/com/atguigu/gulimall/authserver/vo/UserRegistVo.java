package com.atguigu.gulimall.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

// 使用JSR303校验
@Data
public class UserRegistVo {

    @Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
    @NotEmpty(message = "用户名必须提交")
    private String userName;

    @Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
    @NotEmpty(message = "密码必须提交")
    private String password;

    /**
     * 限制11位手机号，其中第一位必须为1，第二为必须在3-9之间，剩下9个数字可以是0-9任意一个
     */
    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
