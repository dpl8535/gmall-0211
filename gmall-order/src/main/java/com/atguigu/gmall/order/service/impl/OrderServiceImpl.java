package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author dplStart
 * @create 上午 08:38
 * @Description
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private SmsGmallClient smsGmallClient;

    @Autowired
    private WmsGmallClient wmsGmallClient;

    @Autowired
    private UmsGmallClient umsGmallClient;

    @Autowired
    private CartGmallClient cartGmallClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "order:token:";

    @Override
    public OrderConfirmVo confirm() {

        // 0.获取到用的登录信息，从而获取到userId
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 到这一步用户必须登录才可以进行结算，所以获取userId即可
        Long userId = userInfo.getUserId();

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        // 1. 获取用所有户地址
        ResponseVo<List<UserAddressEntity>> userAddressVo = umsGmallClient.queryUserAddressByUserId(userId);
        List<UserAddressEntity> userAddressEntities = userAddressVo.getData();
        if (!CollectionUtils.isEmpty(userAddressEntities)) {
            orderConfirmVo.setAddresses(userAddressEntities);
        }

        // 2.获取用户选中的商品
        ResponseVo<List<Cart>> cartVo = cartGmallClient.queryCheckedCartByUserId(userId);
        List<Cart> carts = cartVo.getData();
        if(CollectionUtils.isEmpty(carts)){
            throw  new RuntimeException("你没有选中的购物车记录，请先要勾选购买的商品！");
        }
        List<OrderItemVo> items = carts.stream().map(cart -> {
            OrderItemVo orderItemVo = new OrderItemVo();

            orderItemVo.setSkuId(cart.getSkuId());
            orderItemVo.setCount(cart.getCount());

            ResponseVo<SkuEntity> skuEntityResponseVo = pmsGmallClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            // 如果cart设置不上使用skuEntity
            orderItemVo.setTitle(cart.getTitle());
            orderItemVo.setDefaultImage(cart.getDefaultImage());
            orderItemVo.setPrice(cart.getPrice());

            // 获取到商品重量
            orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));

            // 3.获取商品基本属性
            ResponseVo<List<ItemSaleVo>> itemSalesVo = smsGmallClient.queryItemSalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = itemSalesVo.getData();
            if (!CollectionUtils.isEmpty(itemSaleVos)) {
                orderItemVo.setSales(itemSaleVos);
            }
            // 4.获取商品销售属性
            ResponseVo<List<SkuAttrValueEntity>> skuAttrValuesVo = pmsGmallClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValuesVo.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                orderItemVo.setSaleAttrs(skuAttrValueEntities);
            }

            // 5.获取商品库存
            ResponseVo<List<WareSkuEntity>> wareSkuVo = wmsGmallClient.getWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                orderItemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }


            return orderItemVo;
        }).collect(Collectors.toList());
        orderConfirmVo.setItems(items);

        // 查询用户获取购物积分
        ResponseVo<UserEntity> userEntityResponseVo = umsGmallClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        Integer integration = userEntity.getIntegration();
        orderConfirmVo.setBounds(integration);

        // 6.保存防重token
        String orderToken = IdWorker.getTimeId();
        orderConfirmVo.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken, 3, TimeUnit.HOURS);

        return orderConfirmVo;
    }

    public OrderEntity submit(OrderSubmitVo submitVo) {
        // 1.防重
        String orderToken = submitVo.getOrderToken();
        if (StringUtils.isBlank(orderToken)){
            throw new OrderException("^_^");
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if (!flag){
            throw new OrderException("请不要重复提交！");
        }

        // 2.验价
        BigDecimal totalPrice = submitVo.getTotalPrice();
        List<OrderItemVo> items = submitVo.getItems();
        if (CollectionUtils.isEmpty(items)){
            throw new OrderException("请选择要购买的商品！");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            // 根据skuId查询数据库中的实时单价
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsGmallClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return new BigDecimal(0);
            }
            return skuEntity.getPrice().multiply(item.getCount());
        }).reduce((a, b) -> a.add(b)).get();
        if (totalPrice.compareTo(currentTotalPrice) != 0) {
            throw new OrderException("页面已过期，请刷新后再试！");
        }

        // 3.验库存并锁库存
        List<SkuLockVo> lockVos = items.stream().map(item -> {
            SkuLockVo lockVo = new SkuLockVo();
            lockVo.setSkuId(item.getSkuId());
            lockVo.setCount(item.getCount().intValue());
            return lockVo;
        }).collect(Collectors.toList());
        ResponseVo<List<SkuLockVo>> skuLockResponseVo = this.wmsGmallClient.checkAndLock(lockVos, orderToken);
        List<SkuLockVo> skuLockVos = skuLockResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuLockVos)){
            throw new OrderException(JSON.toJSONString(skuLockVos));
        }

        // 4.下单
        Long userId = null;
        OrderEntity orderEntity = null;
        try {
            UserInfo userInfo = LoginInterceptor.getUserInfo();
            userId = userInfo.getUserId();
            ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.saveOrder(submitVo, userId);
            orderEntity = orderEntityResponseVo.getData();

        } catch (Exception e) {
            e.printStackTrace();
            // 发送消息给库存和oms，解锁库存并修改订单状态
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "order.fail", orderToken);
        }

        // 5.发消息给购物车，删除对应购物车信息
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            map.put("skuIds", JSON.toJSONString(skuIds));
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "cart.delete", map);
        } catch (AmqpException e) {
            e.printStackTrace();
        }

        return orderEntity;
    }
}
