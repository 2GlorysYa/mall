package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

// 不需要再进行数据校验了，因为是auth服务调用member服务，能调用肯定就已经校验过了
@Data
public class MemberRegisterVo {

    private String userName;

    private String password;

    private String phone;

}
