package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;

public interface CartService {

    CartItem addToCart(Long skuId, Integer num);

    /**
     * 获取购物车中的某个购物项
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取购物车
     * @return
     */
    Cart getCart();


    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 改变购物车中商品选中的状态
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物车数量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
