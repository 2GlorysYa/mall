package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 18:51:52
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
