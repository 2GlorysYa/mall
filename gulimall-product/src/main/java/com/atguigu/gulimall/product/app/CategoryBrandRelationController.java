package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.vo.BrandVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-30 15:35:46
 */
@RestController()
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取当前品牌关联的所有分类列表
     * 同时指定这个方法只处理Get请求
     */
    // @RequestMapping("/catelog/list", value = RequestMethod.GET)
    @GetMapping("/catelog/list")
        public R cateloglist(@RequestParam("brandId") Long brandId){
        // 传入一个queryWrapper查询条件，根据id查询，所以从请求参数中截取brandId
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.
                list(new  QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data", data);
    }

    /**
     * 获取某个分类下品牌的关联信息
     * 1。controller：处理请求，接收和校验数据
     * 2。service接收controller传来的数据，进行业务处理
     * 3。controller接收service处理完的数据，封装页面指定的vo并返回
     * @param catId 获取请求参数中的catId
     * @return
     */
    @GetMapping("/brands/list")
    public R relationBrandsList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCatId(catId);

        List<BrandVo> collect = vos.stream().map(item-> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", collect);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){

            categoryBrandRelationService.saveDetail(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
