package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<GroupVo> queryGroupVosByCId(Long cid) {
 /*       List<GroupVo> groupVoList = new ArrayList<>();

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().eq("category_id", cid);
        //根据cId查询出属性组的信息
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(wrapper);

        //根据cId查询属性信息
        for (AttrGroupEntity attrGroupEntity : attrGroupEntities) {
            GroupVo groupVo = new GroupVo();
            BeanUtils.copyProperties(attrGroupEntity, groupVo);
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", groupVo.getId())
                    .eq("type", 1));

            if (attrEntities != null) {
                groupVo.setAttrEntities(attrEntities);
                groupVoList.add(groupVo);
            }
        }
        return groupVoList;*/

        List<GroupVo> groupVoList = new ArrayList<>();

        QueryWrapper<AttrGroupEntity> wrapper =
                new QueryWrapper<AttrGroupEntity>().eq("category_id", cid);
        //根据cId查询出属性组的信息
        List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(wrapper);

        //显示规格下的所有属性值
        return attrGroupEntities.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            BeanUtils.copyProperties(attrGroupEntity,groupVo);
            List<AttrEntity> attrEntityList =
                    this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", attrGroupEntity.getId())
                    .eq("type", 1));
            groupVo.setAttrEntities(attrEntityList);
            return groupVo;
        }).collect(Collectors.toList());

    }

}