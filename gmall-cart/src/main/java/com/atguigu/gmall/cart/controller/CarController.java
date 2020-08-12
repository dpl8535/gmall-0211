package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author dplStart
 * @create 下午 11:37
 * @Description
 */
@Controller
public class CarController {

    @Autowired
    private CartService cartService;

    @GetMapping("user/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartByUserId(@PathVariable("userId") Long userId){
        List<Cart> carts = this.cartService.queryCheckedCartByUserId(userId);
        return ResponseVo.ok(carts);
    }

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

/*    @GetMapping("test")
    @ResponseBody
    public String test() {

        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        System.out.println(userInfo);
        return "hello cart!";
    }*/

    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request) throws ExecutionException, InterruptedException {

        long now = System.currentTimeMillis();
        System.out.println("这是controller的test方法开始执行。。。。。。。。。");
        this.cartService.executor1();
        this.cartService.executor2();
//        this.cartService.executor1().addCallback(
//                t -> System.out.println("异步成功回调executor1：" + t),
//                ex -> System.out.println("异步失败回调executor1：" + ex.getMessage()));
//        this.cartService.executor2().addCallback(
//                t -> System.out.println("异步成功回调executor2：" + t),
//                ex -> System.out.println("异步失败回调executor2：" + ex.getMessage()));
//        System.out.println(future1.get());
//        System.out.println(future2.get());
        System.out.println("这是controller的test方法结束执行。。。。。。。。。" + (System.currentTimeMillis() - now));

        //System.out.println(LoginInterceptor.userInfo);
//        System.out.println(request.getAttribute("userId"));
//        System.out.println(request.getAttribute("userKey"));
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        System.out.println(userInfo);
        return "hello....";
    }
}
