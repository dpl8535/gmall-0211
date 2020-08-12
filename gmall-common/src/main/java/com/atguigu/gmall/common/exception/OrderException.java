package com.atguigu.gmall.common.exception;

/**
 * @author dplStart
 * @create 下午 09:24
 * @Description
 */
public class OrderException extends RuntimeException {
    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
