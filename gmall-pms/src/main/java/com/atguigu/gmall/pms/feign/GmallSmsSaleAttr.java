package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author dplStart
 * @create 上午 10:00
 * @Description
 */
@FeignClient("sms-service")
public interface GmallSmsSaleAttr extends GmallSmsApi {
}
