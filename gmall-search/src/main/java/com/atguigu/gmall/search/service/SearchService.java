package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @author dplStart
 * @create 上午 12:56
 * @Description
 */
public interface SearchService {

    SearchResponseVo search(SearchParamVo searchParamVo);
}
