package com.atguigu.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

// VO 视图对象，用于把前端页面传回来的数据处理并封装成对象返回给视图
// 这里因为前端多需要一个属性attrGroup，但又不能在原先的attrEntity里增加这一属性，于是选择用VO
@Data
public class AttrVo {

    /**
     * 属性id
     */
    @TableId
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 属性图标
     */
    private String icon;
    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;
    /**
     * 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
     */
    private Integer attrType;
    /**
     * 启用状态[0 - 禁用，1 - 启用]
     */
    private Long enable;
    /**
     * 所属分类
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     */
    private Integer showDesc;

    private Long attrGroupId;
}
