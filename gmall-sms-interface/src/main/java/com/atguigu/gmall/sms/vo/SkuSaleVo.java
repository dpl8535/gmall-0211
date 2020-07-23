package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:58
 * @Description
 */
@Data
public class SkuSaleVo {

    private Long skuId;

    //满减活动
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //购物 成长积分
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满几件打几折
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

}
