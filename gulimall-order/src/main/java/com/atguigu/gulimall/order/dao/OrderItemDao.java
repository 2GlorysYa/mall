package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-01-21 23:26:55
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
