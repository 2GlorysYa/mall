package com.atguigu.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public class Cart {

    /**
     * 商品集合
     */
    private List<CartItem> items;

    /*** 商品的数量，有几种不同的商品，保证每次获取属性都会单独计算 */
    private Integer countNum;

    /*** 商品的类型数量，有几种类型的商品 */
    private Integer countType;

    /*** 整个购物车的总价，保证每次获取属性都会单独计算 */
    private BigDecimal totalAmount;

    /*** 减免的价格*/
    private BigDecimal reduce = new BigDecimal("0.00");

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }


    /*** 计算商品的总量
     * 首先判断购物车里是否有商品，如果商品集合不为空，就把每种商品遍历出来并计算数量之和
     * */
    public Integer getCountNum() {
        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                count += item.getCount(); // 获得每种商品的数量 CarItem
            }
        }
        return count;
    }

    /**
     * 获得购物车中商品的种类
     * @return
     */
    public Integer getCountType() {
        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * 计算商品总价值
     * @return
     */
    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if (this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                // 计算总价只考虑购物车中选中的商品
                if(item.getCheck()){
                    // 对每种商品求的他的总价，cartItem里写过这个方法
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        return amount.subtract(this.getReduce());
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
