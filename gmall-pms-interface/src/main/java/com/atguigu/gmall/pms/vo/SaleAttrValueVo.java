package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author dplStart
 * @create 下午 07:46
 * @Description
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
