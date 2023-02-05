package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 18:51:52
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();    // 新增的树形查询方法，需要在接口实现类里实现

    void removeMenuByIds(List<Long> asList);

    /** 找到catelogId的完整路径
     * 父/子/孙
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Catagories();

    Map<String, List<Catelog2Vo>> getCatalogJson();

}

