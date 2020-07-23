package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsSaleAttr;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrValueVo;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private GmallSmsSaleAttr gmallSmsSaleAttr;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo getSpuByCategoryIdAndPageParamVo(Long cId, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (cId != 0) {
            wrapper.eq("category_id", cId);
        }

        String paramVoKey = pageParamVo.getKey();
        if (StringUtils.isNotBlank(paramVoKey)) {
            wrapper.and(t -> t.like("name", paramVoKey).or().like("id", paramVoKey));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }

    @Override
    public void bigSave(SpuVo spuVo) {

        //1.向spu表中插入数据
        //1.1.pms_spu表中插入
        spuVo.setPublishStatus(1);
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
//        this.save(spuVo);
        this.baseMapper.insert(spuVo);

        Long spuId = spuVo.getId();
        //1.2.pms_desc表中插入
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        List<String> spuImages = spuVo.getSpuImages();
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
        this.spuDescMapper.insert(spuDescEntity);

        //1.3.pms_attr_value表中插入
        List<BaseAttrValueVo> baseAttrs = spuVo.getBaseAttrs();

        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> attrValueVoList =
                    baseAttrs.stream().map(baseAttrValueVo -> {
                baseAttrValueVo.setSpuId(spuId);
                baseAttrValueVo.setSort(1);
                return baseAttrValueVo;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(attrValueVoList);
        }


        //2.向sku表中插入数据
        //2.1.向pms_sku表中插入
        List<SkuVo> skus = spuVo.getSkus();
        skus.forEach(skuVo -> {
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(skuVo, skuEntity);
            skuEntity.setBrandId(spuVo.getBrandId());
            skuEntity.setSpuId(spuId);
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            //获取到默认图片
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuEntity.setDefaultImage(skuEntity.getDefaultImage() == null ? images.get(0) : skuEntity.getDefaultImage());
            }
            this.skuMapper.insert(skuEntity);

            //获取到skuId
            Long skuId = skuEntity.getId();

            //2.2.向pms_images中插入
            if (!CollectionUtils.isEmpty(images)) {
                String defaultImage = images.get(0);
                List<SkuImagesEntity> skuImagesEntities =
                        images.stream().map(image -> {

                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);

                    skuImagesEntity.setDefaultStatus(StringUtils.equals(image, defaultImage) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }


            //2.3.向pms_attr_value中插入
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {

                List<SkuAttrValueEntity> skuAttrValueEntities =
                        saleAttrs.stream().map(skuAttrValueEntity -> {
                    skuAttrValueEntity.setSkuId(skuId);
                    return skuAttrValueEntity;

                }).collect(Collectors.toList());
                this.skuAttrValueService.saveBatch(skuAttrValueEntities);
            }

            //3.向sms表中插入数据
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsSaleAttr.saveSkuSale(skuSaleVo);

        });
    }

}