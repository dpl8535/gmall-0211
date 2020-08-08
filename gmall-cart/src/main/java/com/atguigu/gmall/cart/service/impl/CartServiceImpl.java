package com.atguigu.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.PmsGmallClient;
import com.atguigu.gmall.cart.feign.SmsGmallClient;
import com.atguigu.gmall.cart.feign.WmsGmallClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.service.AsyncCartService;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jdk.internal.dynalink.beans.StaticClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dplStart
 * @create 下午 06:46
 * @Description
 */
@Service
public class CartServiceImpl implements CartService {

//    @Autowired
//    private CartMapper cartMapper;

    @Autowired
    private AsyncCartService asyncCartService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private WmsGmallClient wmsGmallClient;

    @Autowired
    private SmsGmallClient smsGmallClient;

    private static final String KEY_PREFIX = "cart:info:";

    private static final String PRICE_PREFIX = "cart:price:";

    @Override
    public void addToCart(Cart cart) {

        // 1.获取到用户的登录状态，根据不同的登录状态存入不同缓存中
        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        String userId = null;
        // 证明用户登录了
        if (userInfo.getUserId() != null) {
            userId = userInfo.getUserId().toString();
        } else {
            //证明用户没有登录
            userId = userInfo.getUserKey();
        }
        // 定义该key是外层key，用来判断是否是否含有该商品{key:{skuId:cart}}
        String key = KEY_PREFIX + userId;

        // 2.判断缓存是否有该商品，有则增加数量，数据库中数量进行更新，缓存中重新put
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(cart.getCount().add(count));

            // 更新数据库
//            this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
            asyncCartService.updateCartByUserIdAndSkuId(userId, cart);
        } else {
            // 3.没有则添加该商品到缓存和数据库中
            cart.setUserId(userId);
            cart.setCheck(true);

            ResponseVo<SkuEntity> skuEntityResponseVo = pmsGmallClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                cart.setTitle(skuEntity.getTitle());
                cart.setDefaultImage(skuEntity.getDefaultImage());
                cart.setPrice(skuEntity.getPrice());
            }

            ResponseVo<List<WareSkuEntity>> wareSkuEntityVo = wmsGmallClient.getWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuEntityVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            ResponseVo<List<ItemSaleVo>> itemSalesVo = smsGmallClient.queryItemSalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSalesVo.getData();
            if (!CollectionUtils.isEmpty(itemSaleVos)) {
                cart.setSales(JSON.toJSONString(itemSaleVos));
            }

            ResponseVo<List<SkuAttrValueEntity>> saleAttrValueVo = pmsGmallClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrValueVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            }

            // 保存到数据库中
//            this.cartMapper.insert(cart);
            this.asyncCartService.addCart(cart);

            // 新增实时价格到缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        // 保存到缓存中// 更新缓存
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    @Override
    public Cart cartDetails(Long skuId) {
        String key = KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        if (userInfo.getUserId() != null) {
            key += userInfo.getUserId();
        } else {
            key += userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        } else {
            throw new RuntimeException("该用户在购物车中不存在该商品");
        }
    }

    @Override
    public List<Cart> queryCarts() {
        // 1.首先获取到用户的登录状态
        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        String unLoginUserKey = userInfo.getUserKey();
        String unLoginKey = KEY_PREFIX + unLoginUserKey;

        // 2.在没有登录的时候，获取到购物车详情
        BoundHashOperations<String, Object, Object> unLoginHashOps = redisTemplate.boundHashOps(unLoginKey);
        List<Object> unLoginCartJsons = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(unLoginCartJsons)) {
            unLoginCarts = unLoginCartJsons.stream().map(
                    unLoginCart -> {
                        Cart cart = JSON.parseObject(unLoginCart.toString(), Cart.class);

                        // 从缓存中获取到当前价格并赋值到购物车中
                        String curPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                        cart.setCurrentPrice(new BigDecimal(curPrice));

                        return cart;
                    }
            ).collect(Collectors.toList());
        }
        // 3.判断用户登录状态
        Long userId = userInfo.getUserId();
        if (userId == null) {
            // 4.返回未登录状态下的购物车信息
            return unLoginCarts;
        }

        // 5.用户登录，获取到未登录下的购物信息，如果与登录下的购物车信息相同则合并该商品
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginCartHashOps = redisTemplate.boundHashOps(loginKey);
//        List<Object> loginCartValues = loginCartHashOps.values();
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                BigDecimal count = cart.getCount();
                if (loginCartHashOps.hasKey(cart.getSkuId().toString())) {
                    String loginCarts = loginCartHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(loginCarts, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    this.asyncCartService.updateCartByUserIdAndSkuId(userId.toString(), cart);
                } else {
                    cart.setUserId(userId.toString());
                    this.asyncCartService.addCart(cart);
                }
                loginCartHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
        }
        // 6.删除未登录状态下的购物车商品
        redisTemplate.delete(unLoginKey);
        this.asyncCartService.deleteCartsByUserId(unLoginUserKey);

        // 7.查询登录状态下的购物车
        List<Object> loginCartJsons = loginCartHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(cartJson -> {
                        Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                        String curPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                        cart.setCurrentPrice(new BigDecimal(curPrice));
                        return cart;
                    }
            ).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void updateCart(Cart cart) {

        // 1.获取userKey
        UserInfo userInfo = LoginInterceptor.getThreadLocal();

        String userId = null;
        // 2.封装userKey
        if (userInfo.getUserId() != null) {
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }

        String key = KEY_PREFIX + userId;

        // 3.根据key获取到缓存中的购物车商品,根据外层的可以获取到所有购物车商品
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        BigDecimal count = cart.getCount();

        // 4.根据skuId即内层key判断是否有对应的商品
        if (hashOps.hasKey(cart.getSkuId().toString())) {

            // 5.获取到skuId对应的商品，但是是json字符串，所以要反序列化
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);

            // 6.更改数量
            cart.setCount(count);
            // 7.修改数据库
            this.asyncCartService.updateCartByUserIdAndSkuId(userId, cart);
            // 8.修改缓存
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }

    }

    @Override
    public void deleteCart(Long skuId) {
        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        String userId = null;
        if (userInfo.getUserId() != null) {
            userId = userInfo.getUserId().toString();
        } else {
            userId = userInfo.getUserKey();
        }
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
            this.asyncCartService.deletecartsBySkuIdAndUserId(userId, skuId);
        }
    }
}
