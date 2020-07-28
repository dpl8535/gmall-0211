package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 10:53
 * @Description
 */
@FeignClient("wms-service")
public interface WmsGmallClient extends GmallWmsApi {
}
