package com.atguigu.gmall.ums.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 10:22
 * @Description
 */
@FeignClient("ums-service")
public interface UmsGmallClient extends GmallUmsApi {
}
