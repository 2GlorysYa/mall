package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 品牌
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 20:32:43
 */
@RestController // controller + responsebody
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
        public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand", brand);


    }

    /**
     * 保存
     * 前端传过来的表单需要在后端进行二次校验，开启校验：添加@Valid注解
     * 但@Valid注解无法指定校验分组(valid是javax的注解，validated是spring注解)，于是使用@Validated注解, 指定这个方法执行AddGroup组的校验
     */
    @RequestMapping("/save")
        public R save(@Validated(value = AddGroup.class) @RequestBody BrandEntity brand /*, BindingResult result*/, BindingResult result){
        // 绑定一个校验结果BindingResult，如果校验有错，就封装错误信息并返回，如果没错，就保存
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((item) -> { // 把每个字段的error都封装在map
                String message = item.getDefaultMessage();  // 获取错误提示，这个信息在校验注解中可以自定义为message
                String field = item.getField(); // 获取错误的属性名
                map.put(field, message);
            });
            // 返回一个R对象封装error信息，是个map，又put进去了一个map对象错误信息（提示+属性名）
            // 最终postman查看响应返回的都是JSON，key-value
            return R.error(400, "提交的数据不合法").put("data", map);
        } else {
            brandService.save(brand);
        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     * 指定该方法执行updateGroup组的校验
     */
    @RequestMapping("/update")
        public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态, 只用判断showStatus对不对，不去验证其他信息, 所以需要单独写个方法
     * 否则@ListValue(vals={0,1}, groups = {AddGroup.class, UpdateGroup.class})
     * 会同时验证其他字段
     * @param brand
     * @return
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
