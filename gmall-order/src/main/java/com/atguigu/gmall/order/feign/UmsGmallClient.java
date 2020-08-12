package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 上午 12:58
 * @Description
 */
@FeignClient("ums-service")
public interface UmsGmallClient extends GmallUmsApi {
}
