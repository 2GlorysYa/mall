package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {


    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal totalPrice;

    /**
     *  价格
     *  因为涉及到价格计算，所以需要用bigDecimal表示精确的小数
     * */
    private BigDecimal price;

    /*** 数量*/
    private Integer count;

    // private Boolean hasStock;

    private BigDecimal weight;
}
