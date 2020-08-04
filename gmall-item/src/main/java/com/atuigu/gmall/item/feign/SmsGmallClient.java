package com.atuigu.gmall.item.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author dplStart
 * @create 下午 05:36
 * @Description
 */
@FeignClient("sms-service")
public interface SmsGmallClient extends GmallSmsApi {
}
