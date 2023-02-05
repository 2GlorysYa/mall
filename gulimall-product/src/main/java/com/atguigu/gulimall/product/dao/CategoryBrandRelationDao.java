package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-30 15:35:46
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    // Dao声明的接口方法如果有两个以上的参数，就必须为每一个参数声明@Param, 用来写sql时引用catId
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
