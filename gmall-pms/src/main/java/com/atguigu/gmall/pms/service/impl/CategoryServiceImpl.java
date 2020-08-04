package com.atguigu.gmall.pms.service.impl;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCateGoryByParentId(Long parentId) {

        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();

        if (parentId != -1) {
            wrapper.eq("parent_id", parentId);
        }
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(wrapper);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> getCategoriesWitSubs(long pid) {
        List<CategoryEntity> categoryEntities = categoryMapper.getCategoriesWitSubs(pid);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> query123CategoriesByCid3(Long cid) {

        //根据三级分类id查询到三级分类信息
        CategoryEntity level3categoryEntity = this.categoryMapper.selectById(cid);

        //根据三级父id查询到二级分类信息
        CategoryEntity level2CategoryEntity = this.categoryMapper.selectById(level3categoryEntity.getParentId());

        //根据二级分类父id查询一级分类信息
        CategoryEntity level1CategoryEntity = this.categoryMapper.selectById(level2CategoryEntity.getParentId());

        return Arrays.asList(level1CategoryEntity,level2CategoryEntity,level3categoryEntity);
    }

}