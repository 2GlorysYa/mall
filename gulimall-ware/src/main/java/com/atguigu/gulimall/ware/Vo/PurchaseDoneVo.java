package com.atguigu.gulimall.ware.Vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {

    @NotNull    // 数据校验
    private Long id;//采购单id

    private List<PurchaseItemDoneVo> items;
}
