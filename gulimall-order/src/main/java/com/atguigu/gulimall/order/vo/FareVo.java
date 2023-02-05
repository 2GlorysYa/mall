package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Yibo Lei
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
