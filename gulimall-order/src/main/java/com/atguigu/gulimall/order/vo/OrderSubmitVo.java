package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Yibo Lei
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {

    private Long attrId;   // 收货地址id
    private Integer payType; // 支付方式

    private String orderToken;  // 防重令牌
    private BigDecimal payPrice; // 应付价格

    private String note; // 备注信息

}
