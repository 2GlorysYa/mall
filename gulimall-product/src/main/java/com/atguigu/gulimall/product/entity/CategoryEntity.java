package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 商品三级分类
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 18:51:52
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类id
	 */
	@TableId
	private Long catId;
	/**
	 * 分类名称
	 */
	private String name;
	/**
	 * 父分类id
	 */
	private Long parentCid;
	/**
	 * 层级
	 */
	private Integer catLevel;
	/**
	 * 是否显示[0-不显示，1显示]
	 * 逻辑删除字段，意思是通过MP的方法来删除和查找不会真正物理上删除数据表数据，而是改变这个字段的值实现"逻辑上的删除"
	 * 但我们这张pms_category表里, 默认是1表示显示，0显示，所以需要更改规则为value=1 可显示 deval = 0 不显示
	 */
	@TableLogic(value = "1", delval = "0")
	private Integer showStatus;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 图标地址
	 */
	private String icon;
	/**
	 * 计量单位
	 */
	private String productUnit;
	/**
	 * 商品数量
	 */
	private Integer productCount;


	/**
	 * 所有的子分类，因为他不是数据表原有的属性，而是单独加上去的，所以声明为TableField并且不存在表中
	 * 上面所有数据会被统一封装在children里，如果children为空，就不要把他序列化到响应体里返回
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@TableField(exist = false)
	private List<CategoryEntity> children;
}
