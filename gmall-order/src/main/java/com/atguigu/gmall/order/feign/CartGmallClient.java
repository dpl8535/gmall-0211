package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 上午 12:58
 * @Description
 */
@FeignClient("cart-service")
public interface CartGmallClient extends GmallCartApi {
}
