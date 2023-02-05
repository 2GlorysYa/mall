package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 18:51:52
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 * id字段对于更新的时候去验证Not Null，对于新增的时候去验证Null
	 */
	@NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 * 验证，至少包含一个非空字符, 错误提示信息
	 */
	@NotBlank(message = "品牌名必须提交", groups={AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 * url不能为空
	 * 新增时不能为空
	 * 但update时不触发NotBlank校验，意思是可以不修改
	 */
	@NotBlank(message = "logo不能为空", groups = {AddGroup.class})
	@URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals={0,1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 * 想要校验首字符位于A-Z，没有现成的注解，这种情况都使用自定义注解@pattern，参数为正则表达式
	 * 无论修改或新增，都必须携带一个字符
	 */
	@NotEmpty(message = "不能为空", groups = {AddGroup.class}) // 新增的时候都不能为空
	@Pattern(regexp = "^[A-Za-z]+$", message="检索首字母必须是一个字符", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 * sort字段最小值是0, 校验注解用min
	 * 注意sort是Integer，所以不能用NotEmpty，文档有具体支持的类型，使用前查看
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
