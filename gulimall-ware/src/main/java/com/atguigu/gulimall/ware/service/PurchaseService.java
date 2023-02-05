package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.Vo.MergeVo;
import com.atguigu.gulimall.ware.Vo.PurchaseDoneVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author leiyibo
 * @email ryanyibo45@outlook.com
 * @date 2022-02-02 13:09:35
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo doneVo);
}

