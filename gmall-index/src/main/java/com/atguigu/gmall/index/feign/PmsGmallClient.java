package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 10:15
 * @Description
 */
@FeignClient(value = "pms-service")
public interface PmsGmallClient extends GmallPmsApi {
}
