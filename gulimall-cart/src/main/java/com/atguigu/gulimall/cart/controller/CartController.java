package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentCartItems() {
        return cartService.getUserCartItems();
    }

    /**
     * 删除购物车中的商品
     * @return
     */
    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        // 删除完了，也是刷新本页
        return "redirect:http://cart.gulimall.com/addCartSuccess.html";
    }


    /**
     * 计算选中商品的数量, 点击并修改购物车里的商品数量会发送请求
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {

        cartService.changeItemCount(skuId, num);
        // 改变完了，再重定向回当前页面，就是刷新
        return "redirect:http://cart.gulimall.com/addCartSuccess.html";
    }


    /**
     * 在购物车添加成功的页面（就是购物车页面）进行选中商品的操作
     * 选中以后执行刷新
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        // 最后重定向回添加到购物车成功的页面，相当于刷新了本页面
        return "redirect:http://cart.gulimall.com/addCartSuccess.html";
    }

    /**
     * 浏览器有一个cookie：user-key，用来标识用户身份，一个月后过期
     * 如果第一次使用京东的购物车功能，都会给一个临时的用户身份，浏览器会保存这个cookie
     * 之后每次访问都会带上这个cookie
     * 已登录的情况下去session查
     * 未封路的情况下，如果没有临时用户，还需要帮忙创建一个临时用户
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        // 1。快速得到用户信息， id， user-key
        // UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * addFlashAttribute是模拟session，所以数据不会出现在url后, 但是只能取一次
     * addAttribute将数据放在url中
     * @param skuId 要添加到购物车的商品id
     * @param num   要添加的商品数量
     * @param redirectAttributes
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes redirectAttributes)  // 重定向数据，会自动将参数追加到url后面
            throws ExecutionException, InterruptedException {

        // 添加数量到用户购物车
        cartService.addToCart(skuId, num);
        // 返回skuId告诉哪个添加成功了
        redirectAttributes.addAttribute("skuId", skuId);

        // 重定向到成功页面
        return "redirect:http://cart.gulimall.com/addCartSuccess.html";
    }


    /**
     * 如果使用addFlashAttribute将参数携带到重定向的页面，则必须使用@RequestrParam（）获取
     * 因为他不是显式追加在url上，只有addAttribute方法才是显式放在链接后面
     * @param skuId  获取RedirectAttribute中携带的参数
     * @param model
     * @return
     */
    @GetMapping("/addCartSuccess")
    public String addCartItemSuccess(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到了成功页面，再次查询购物车数据即可
        CartItem cartItemVo = cartService.getCartItem(skuId);
        // 查询的结果数据需要返回页面，因此需要一个model
        // 页面可以提取出key为cartItem的数据
        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }
}
