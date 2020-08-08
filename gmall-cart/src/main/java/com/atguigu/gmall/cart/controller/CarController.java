package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 11:37
 * @Description
 */
@Controller
public class CarController {

    @Autowired
    private CartService cartService;

    /**
     * 思路：先把商品添加到购物车，再把获取到购物车的商品显示到cart.html
     *
     * @return
     */

    @GetMapping
    public String addToCart(Cart cart) {
        //调用service中addToCart方法添加商品到购物车
        if (cart == null) {
            throw new RuntimeException("该商品不存在");
        }
        cartService.addToCart(cart);
        return "redirect:http://cart.gmall.com/addCart?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart")
    public String cartValue(@RequestParam("skuId") Long skuId, Model model) {
        Cart cart = cartService.cartDetails(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String cart(Model model){
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts);
        return "cart";
    }

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo<Object> updateCart(@RequestBody Cart cart){
        this.cartService.updateCart(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo<Object> deleteCart(@RequestParam("skuId") Long skuId){ //注意请求方式不同使用的注解不同，该参数不是从路径传过来的,而且请求方式为post，所以使用@RequestParam
        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @GetMapping("test")
    @ResponseBody
    public String test() {

        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        System.out.println(userInfo);
        return "hello cart!";
    }
}
