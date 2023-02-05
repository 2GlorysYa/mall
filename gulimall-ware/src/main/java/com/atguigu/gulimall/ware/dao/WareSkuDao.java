package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 23:39:06
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    // mytais的映射接口的方法中如果有多个参数，就必须要为每一个参数声明@Param
    // 因为xml里对参数名的引用是直接引用@Param里的String
    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum")Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);
}
