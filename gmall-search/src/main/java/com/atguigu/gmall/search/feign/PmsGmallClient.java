package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 10:53
 * @Description
 */
@FeignClient("pms-service")
public interface PmsGmallClient extends GmallPmsApi {
}
