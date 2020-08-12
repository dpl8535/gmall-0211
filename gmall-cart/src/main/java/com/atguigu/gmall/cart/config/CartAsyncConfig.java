package com.atguigu.gmall.cart.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @author dplStart
 * @create 下午 07:57
 * @Description
 */
@Configuration
public class CartAsyncConfig implements AsyncConfigurer {

    @Autowired
    private CartAsyncExceptionHandler cartAsyncExceptionHandler;

    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return cartAsyncExceptionHandler;
    }
}
