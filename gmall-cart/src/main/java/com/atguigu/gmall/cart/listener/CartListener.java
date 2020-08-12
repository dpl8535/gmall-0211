package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.PmsGmallClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.ws.BindingType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author dplStart
 * @create 下午 11:22
 * @Description
 */
@Component
@Slf4j
public class CartListener {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";

    private static final String KEY_PREFIX = "cart:info:";

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "CART-ITEM-QUEUE", durable = "true"),
                    exchange = @Exchange(value = "PMS-ITEM-EXCHANGE", durable = "true", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
                    key = {"item.update"}
            )
    )
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        List<SkuEntity> skuEntities = pmsGmallClient.getSkuBySpuId(spuId).getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {
            skuEntities.forEach(skuEntity -> {
                this.redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
            });
        }
        //确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-CART-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteCart(Map<String, Object> map, Channel channel, Message message) throws IOException {
        if (CollectionUtils.isEmpty(map)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return ;
        }
        try {
            String userId = map.get("userId").toString();
            String skuIdString = map.get("skuIds").toString();
            List<String> skuIds = JSON.parseArray(skuIdString, String.class);
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            hashOps.delete(skuIds.toArray());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()){
                log.error("下单成功之后删除购物车失败，消息内容：{}", map.toString());
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
