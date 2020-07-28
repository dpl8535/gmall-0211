package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 上午 10:00
 * @Description
 */
@FeignClient("sms-service")
public interface GmallSmsSaleAttr extends GmallSmsApi {
}
