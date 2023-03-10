package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.common.utils.R;



/**
 * 商品三级分类
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 20:32:43
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以[[树形结构]]组装起来
     */
    @RequestMapping("/list/tree")    // 查出所有分类
        public R list(){
        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("page", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
        public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改自己的同时也要连带修改分类品牌关系表中的数据
     *
     */
    @RequestMapping("/update")
        public R update(@RequestBody CategoryEntity category){
		categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     * @RequestBody：获取请求体 ，所以必须发送Post请求，因为get请求没有请求体
     * Spring mvc自动将请求体的数据（json），转换为对应的对象
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] catIds){
        // 1。检查当前删除的菜单，是否被别的地方引用
		// categoryService.removeByIds(Arrays.asList(catIds));
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();

    }

    /**
     * 批量修改分类
     * @param category
     * @return
     */
    @RequestMapping("/update/sort")
    public R updateBySort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

}
