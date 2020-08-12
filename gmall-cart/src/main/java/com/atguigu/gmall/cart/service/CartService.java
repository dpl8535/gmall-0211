package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.pojo.Cart;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 06:45
 * @Description
 */
public interface CartService {
    void addToCart(Cart cart);

    Cart cartDetails(Long skuId);

    List<Cart> queryCarts();

    void updateCart(Cart cart);

    void deleteCart(Long skuId);

    String executor1();

    String executor2() throws InterruptedException;

    List<Cart> queryCheckedCartByUserId(Long userId);
}
