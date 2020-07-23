package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:58
 * @Description
 */
@Data
public class SkuVo extends SkuEntity {

    //图片集合在其中去默认值
    List<String> images;

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

    //销售属性
    private List<SkuAttrValueEntity> saleAttrs;
}
