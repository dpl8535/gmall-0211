package com.atguigu.gmall.common.exception;

/**
 * @author dplStart
 * @create 下午 09:49
 * @Description
 */
public class UserException extends RuntimeException {
    public UserException() {
    }

    public UserException(String message) {
        super(message);
    }
}
