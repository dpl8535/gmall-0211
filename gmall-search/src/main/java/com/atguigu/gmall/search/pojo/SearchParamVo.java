package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author dplStart
 * @create 上午 12:48
 * @Description
 */
@Data
public class SearchParamVo {

    private String keyword;

    private List<Long> brandId;
    private List<Long> cid;
    private List<String> props;

    private Integer sort; // 1-价格升序 2-价格降序 3-销量降序 4-新品排序

    private Double priceFrom;
    private Double priceTo;

    private Integer pageNo = 1;
    private final Integer pageSize = 20;

    private Boolean store;

}
