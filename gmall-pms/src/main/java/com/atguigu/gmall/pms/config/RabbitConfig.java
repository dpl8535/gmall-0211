package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;

/**
 * @author dplStart
 * @create 下午 04:51
 * @Description
 */

/**
 * 判断消息是否发送成功
 */
@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        //判断消息是否发送到交换机的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.error("消息发送交换机成功");
            } else {
                log.error("消息发送交换机失败：{}", cause);
            }
        });

        //判断消息是否到达队列的回调,没有消息就是好消息
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("消息未到达的交换机:{},路由键:{},消息内容：{}", exchange,routingKey,new String(message.getBody()));
        });

    }

}
