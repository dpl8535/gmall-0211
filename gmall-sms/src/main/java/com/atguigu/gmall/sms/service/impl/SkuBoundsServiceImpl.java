package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );
        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveSkuSale(SkuSaleVo skuSaleVo) {
        //成长积分
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(work)) {
            skuBoundsEntity.setWork(work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3) * 1);
        }
        this.baseMapper.insert(skuBoundsEntity);

        //满减活动
        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, fullReductionEntity);
        this.skuFullReductionMapper.insert(fullReductionEntity);

        //打折活动
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        this.skuLadderMapper.insert(skuLadderEntity);

    }

    @Override
    public List<ItemSaleVo> queryItemSalesBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        //获取积分信息
        SkuBoundsEntity boundsEntity = this.baseMapper.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (boundsEntity != null) {
            ItemSaleVo bound = new ItemSaleVo();
            bound.setType("送积分");
            bound.setDesc("送" + boundsEntity.getGrowBounds() + "成长积分，送" + boundsEntity.getBuyBounds() + "购物积分");
            itemSaleVos.add(bound);
        }

        //获取满减信息
        SkuFullReductionEntity fullReductionEntity = this.skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (fullReductionEntity != null){
            ItemSaleVo fullReduction = new ItemSaleVo();
            fullReduction.setDesc("满减");
            fullReduction.setType("满" + fullReductionEntity.getFullPrice() + "减" + fullReductionEntity.getReducePrice());
            itemSaleVos.add(fullReduction);
        }

        //获取打折信息
        SkuLadderEntity ladderEntity = this.skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (ladderEntity != null){
            ItemSaleVo skuLadder = new ItemSaleVo();
            skuLadder.setType("打折");
            skuLadder.setDesc("满" + ladderEntity.getFullCount() + "件打" + ladderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            itemSaleVos.add(skuLadder);
        }
        return itemSaleVos;
    }
}