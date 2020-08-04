package com.atuigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 05:35
 * @Description
 */
@FeignClient("wms-service")
public interface WmsGmallClient extends GmallWmsApi {
}
