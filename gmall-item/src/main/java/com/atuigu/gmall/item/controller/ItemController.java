package com.atuigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atuigu.gmall.item.service.ItemService;
import com.atuigu.gmall.item.vo.ItemVo;
import com.baomidou.mybatisplus.extension.api.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author dplStart
 * @create 下午 06:27
 * @Description
 */
@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

/*    @GetMapping("{skuId}.html")
    @ResponseBody
    public ResponseVo<ItemVo> loadData(@PathVariable("skuId") Long skuId){
        ItemVo itemVo = itemService.loadDta(skuId);
        return ResponseVo.ok(itemVo);
    }*/

//    @ResponseBody
    @GetMapping("{skuId}.html")
    public String loadData(@PathVariable("skuId") Long skuId, Model model){
        ItemVo itemVo = itemService.loadDta(skuId);
        model.addAttribute("itemVo",itemVo);
        return "item";
    }
}
