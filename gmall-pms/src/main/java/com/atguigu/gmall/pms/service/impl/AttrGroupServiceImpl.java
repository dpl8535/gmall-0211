package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.checkerframework.checker.units.qual.A;
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

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

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
            BeanUtils.copyProperties(attrGroupEntity, groupVo);
            List<AttrEntity> attrEntityList =
                    this.attrMapper.selectList(new QueryWrapper<AttrEntity>()
                            .eq("group_id", attrGroupEntity.getId())
                            .eq("type", 1));
            groupVo.setAttrEntities(attrEntityList);
            return groupVo;
        }).collect(Collectors.toList());

    }

    @Override
    public List<ItemGroupVo> queryGroupWithAttrValue(Long cid, Long spuId, Long skuId) {

        //1.根据cid获取到group列表
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        //2.遍历group列表
        if (CollectionUtils.isEmpty(groupEntities)) {
            return null;
        }
       return groupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setGroupId(group.getId());
            itemGroupVo.setGroupName(group.getName());

            //3.根据group_id 获取到attr列表
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", group.getId()));

            //**要把获取到的值返回给前端，要不然会报groupName为空，切莫忘记重启pms**
            if (CollectionUtils.isEmpty(attrEntities)) {
                return itemGroupVo;
            }
            //4.遍历attrEntities获取到attrIds
            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
            System.out.println("attrIds = " + attrIds);

            List<AttrValueVo> attrValueVos = new ArrayList<>();

            //5.根据attrIds和spuId获取到通用属性列表
            List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));

            if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                //6.遍历spuAttrValueEntities,把其中的值赋值给attrValueVo
                List<AttrValueVo> spuAttrValueVos = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList());
                attrValueVos.addAll(spuAttrValueVos);
            }


            //7.根据attIds和skuId获取到skuAttrValueEntities
            List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                //8.遍历skuAttrValueEntities,并赋值给attrValueVo
                List<AttrValueVo> skuAttrValueVos = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                    return attrValueVo;
                }).collect(Collectors.toList());
                attrValueVos.addAll(skuAttrValueVos);
            }
            itemGroupVo.setAttrs(attrValueVos);
            return itemGroupVo;
        }).collect(Collectors.toList());


    }

}