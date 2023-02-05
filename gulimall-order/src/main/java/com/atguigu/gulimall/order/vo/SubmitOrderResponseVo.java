package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author Yibo Lei
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;  // 如果下单成功，则拿到订单信息

    private Integer code;   // 状态码 0 1
}
