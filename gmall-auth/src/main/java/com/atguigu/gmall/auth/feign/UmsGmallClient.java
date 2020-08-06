package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 上午 08:50
 * @Description
 */
@FeignClient("ums-service")
public interface UmsGmallClient extends GmallUmsApi {
}
