package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传过来的页面数据
 * 需要约定url中参数的格式
 *
 *  catalog3Id=225&keyword=小米&sort=saleCount_asc
 *  即从首页三级菜单中点击手机类会跳转到搜索页，此时url中带上手机对应的catalog3Id
 *  再从搜索页中的搜索框输入小米，并选择一个排序条件，比如为按销量升序排序
 *  那么点击搜索后url中会拼接keyword和sort两个参数去查询
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 品牌id,可以【多选】
     * 在手机页面中选择品牌对应的图片，url会拼接上brandId
     * 如果选择多个品牌，就拼接brandId=1&brandId=2&...
     */
    private List<Long> brandId;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * 是否显示有货 0 / 1
     */
    private Integer hasStock;

    /**
     * 价格区间查询，多种过滤条件
     * 自定义规则
     * 1_500 1到500
     * _500 500以内
     * 500_ 500以上
     */
    private String skuPrice;

    /**
     * 按照属性进行筛选，可以【多选】
     * 自定义规则
     * 系统是1号属性，，在系统一栏里选择其他，对应url为 ...&attrs=1_其他
     * 并选上安卓, &attrs=1_其他:安卓
     */
    private List<String> attrs;

    /**
     * 页码，需要分页，因为客户会点击下一页数据
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;
}
