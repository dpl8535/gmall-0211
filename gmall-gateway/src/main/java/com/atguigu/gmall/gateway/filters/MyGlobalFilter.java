package com.atguigu.gmall.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author dplStart
 * @create 下午 06:13
 * @Description
 */
@Component
@Order(1) //也可以使用该注解代替实现ordered接口
public class MyGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("通过了全局过滤器，所用经过网关的请求都会被拦截！");
        return chain.filter(exchange);
    }

    /**
     * 当配置多个全局过滤器时根据返回值的大小判断实行顺序，值越小优先级越高
     * @return
     */
//    @Override
//    public int getOrder() {
//        return 1;
//    }
}
