package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 05:34
 * @Description
 */
@FeignClient("pms-service")
public interface PmsGmallClient extends GmallPmsApi {
}
