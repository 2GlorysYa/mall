package com.atguigu.gulimall.order.controller;


import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        // 想要把订单确认数据放到页面展示，所以传一个model，springboot会自动new一个出来
        // 再封装到这个model里
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 下单功能
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo) {
        // 去创建订单，验证令牌，验证价格，锁库存...
        // 下单成功来支付选择页
        // 下单失败回到订单确认页重新确认信息
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        System.out.println("订单提交的数据" + vo);
        // 如果下单成功，来到支付页
        if (responseVo.getCode() == 0) {
            return "pay";
        } else {
            // 否则重定向回订单确认页
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
