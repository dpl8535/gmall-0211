package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author dplStart
 * @create 上午 10:52
 * @Description
 */
public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/skuSale/save")
    public ResponseVo<Object> saveSkuSale(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("sms/skubounds/sale/{skuId}")
    public ResponseVo<List<ItemSaleVo>> queryItemSalesBySkuId(@PathVariable("skuId") Long skuId);
}
