package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 10:07
 * @Description
 */
@FeignClient("oms-service")
public interface GmallOmsClient extends GmallOmsApi {
}
