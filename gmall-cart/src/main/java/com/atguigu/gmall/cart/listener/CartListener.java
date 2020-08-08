package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.PmsGmallClient;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.ws.BindingType;
import java.io.IOException;
import java.util.List;

/**
 * @author dplStart
 * @create 下午 11:22
 * @Description
 */
@Component
public class CartListener {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";

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
}
