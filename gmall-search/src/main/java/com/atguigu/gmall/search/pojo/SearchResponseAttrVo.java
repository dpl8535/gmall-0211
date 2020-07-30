package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 07:37
 * @Description
 */
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
