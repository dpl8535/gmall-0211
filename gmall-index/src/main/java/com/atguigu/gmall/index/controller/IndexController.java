package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:08
 * @Description
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    /**
     * 查询一级分类列表，调用pms中的方法根据父id为0查询
     * @param model
     * @return
     */
    @GetMapping
    public String index(Model model){
        List<CategoryEntity> categories = indexService.queryLevel1Category();
        model.addAttribute("categories", categories);
        return "index";
    }

    /**
     * 根据pid获取到二级和三级类别，使用到嵌套查询
     * @param pid
     * @return
     */
    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLevel2And3Category(@PathVariable("pid") Long pid){
        List<CategoryEntity> categoryEntities = indexService.queryLevel2And3Category(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("index/testLock")
    @ResponseBody
    public ResponseVo<Object> testLock(){
        this.indexService.testLock();
        return ResponseVo.ok();
    }


}
