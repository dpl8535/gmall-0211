package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.pojo.Cart;

/**
 * @author dplStart
 * @create 下午 09:27
 * @Description
 */
public interface AsyncCartService {

    public void updateCartByUserIdAndSkuId(String userId, Cart cart);

    public void addCart(String userId, Cart cart);

    void deleteCartsByUserId(String userId);

    void deletecartsBySkuIdAndUserId(String userId, Long skuId);
}
