package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.AsyncCartService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public void updateCartByUserIdAndSkuId(String userId, Cart cart) {
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void addCart(String userId, Cart cart) {
        System.out.println("下方有个异常！");
//        int i = 10 / 0;
        this.cartMapper.insert(cart);
    }

    @Override
    @Async
    public void deleteCartsByUserId(String userId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));
    }

    @Override
    @Async
    public void deletecartsBySkuIdAndUserId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }


}
