package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dplStart
 * @create 上午 12:53
 * @Description
 */
@RestController
public class SearchController {

    @Autowired
    SearchService searchService;

    @GetMapping("/search")
    public ResponseVo<Object> search(SearchParamVo searchParamVo){
        this.searchService.search(searchParamVo);
        return ResponseVo.ok(null);
    }

}
