package com.atguigu.gmall.index.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.PmsGmallClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:20
 * @Description
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Override
    public List<CategoryEntity> queryLevel1Category() {
        ResponseVo<List<CategoryEntity>> categoriesByParentIdVo = pmsGmallClient.getCategoriesByParentId(0l);
        return categoriesByParentIdVo.getData();
    }

    @Override
    public List<CategoryEntity> queryLevel2And3Category(Long pid) {
        ResponseVo<List<CategoryEntity>> categoriesWitSubs = pmsGmallClient.getCategoriesWitSubs(pid);
        List<CategoryEntity> categoryEntities = categoriesWitSubs.getData();
        return categoryEntities;
    }
}
