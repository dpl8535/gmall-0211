package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.OrderConfirmVo;

/**
 * @author dplStart
 * @create 上午 08:38
 * @Description
 */
public interface OrderService {
    OrderConfirmVo confirm();

    OrderEntity submit(OrderSubmitVo submitVo);
}
