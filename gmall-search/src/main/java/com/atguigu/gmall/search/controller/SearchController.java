package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dplStart
 * @create 上午 12:53
 * @Description
 */
//@RestController
@Controller
public class SearchController {

    @Autowired
    SearchService searchService;

    /*@GetMapping("/search")
    public ResponseVo<SearchResponseVo> search(SearchParamVo searchParamVo){
        SearchResponseVo searchResponseVo = this.searchService.search(searchParamVo);
        return ResponseVo.ok(searchResponseVo);
    }*/

    @GetMapping("/search")
    public String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo searchResponseVo = this.searchService.search(searchParamVo);
        model.addAttribute("response", searchResponseVo);
        model.addAttribute("searchParam", searchParamVo);
        return "search";
    }

}
