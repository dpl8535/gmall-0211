package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.AsyncCartService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author dplStart
 * @create 下午 09:27
 * @Description
 */
@Service
public class AsyncCartServiceImpl implements AsyncCartService {

    @Autowired
    private CartMapper cartMapper;
    @Async
    public void updateCartByUserIdAndSkuId(String userId, Cart cart){
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void addCart(Cart cart){
        this.cartMapper.insert(cart);
    }

    @Override
    public void deleteCartsByUserId(String userId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
    }

    @Override
    public void deletecartsBySkuIdAndUserId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

}
