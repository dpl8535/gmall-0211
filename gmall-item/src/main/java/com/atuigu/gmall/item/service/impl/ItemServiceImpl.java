package com.atuigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atuigu.gmall.item.config.ThreadPoolConfig;
import com.atuigu.gmall.item.feign.PmsGmallClient;
import com.atuigu.gmall.item.feign.SmsGmallClient;
import com.atuigu.gmall.item.feign.WmsGmallClient;
import com.atuigu.gmall.item.service.ItemService;
import com.atuigu.gmall.item.vo.ItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ParameterMetaData;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author dplStart
 * @create 下午 06:28
 * @Description
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private WmsGmallClient wmsGmallClient;

    @Autowired
    private SmsGmallClient smsGmallClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public ItemVo loadDta(Long skuId) {

        ItemVo itemVo = new ItemVo();
        CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //1.获取到中间核心部分
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsGmallClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            //2.获取到三级分类信息
            List<CategoryEntity> categoryEntities = pmsGmallClient.query123CategoriesByCid3(skuEntity.getCatagoryId()).getData();//cid
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                itemVo.setCategories(categoryEntities);
            }
        }), threadPoolExecutor);

        CompletableFuture<Void> brandCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            //3.获取到品牌信息
            BrandEntity brandEntity = pmsGmallClient.queryBrandById(skuEntity.getBrandId()).getData();//brandId
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }), threadPoolExecutor);

        CompletableFuture<Void> spuCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            //4.获取到spu信息
            SpuEntity spuEntity = pmsGmallClient.querySpuById(skuEntity.getSpuId()).getData();//spuId
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }), threadPoolExecutor);

        //获取spu下的描述信息
        CompletableFuture<Void> spuDescCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            SpuDescEntity spuDescEntity = pmsGmallClient.querySpuDescById(skuEntity.getSpuId()).getData();
            if (spuDescEntity != null) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }), threadPoolExecutor);

        //5.获取到sku图片列表信息
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntityList = pmsGmallClient.queryImagesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuImagesEntityList)) {
                itemVo.setImages(skuImagesEntityList);
            }
        }, threadPoolExecutor);


        //6.sku营销信息
        CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
            List<ItemSaleVo> itemSaleVoList = smsGmallClient.queryItemSalesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(itemSaleVoList)) {
                itemVo.setSales(itemSaleVoList);
            }
        }, threadPoolExecutor);


        //7.是否有货
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            List<WareSkuEntity> wareSkuEntities = wmsGmallClient.getWareSkuBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                //判断是否有库存用库存量减去锁住的库存量
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        //8.用户选择销售属性集合
        CompletableFuture<Void> saleAttrCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            List<SaleAttrValueVo> saleAttrValueVoList = pmsGmallClient.querySaleItemAttrValueBySpuId(skuEntity.getSpuId()).getData();
            if (!CollectionUtils.isEmpty(saleAttrValueVoList)) {
                itemVo.setSaleAttrs(saleAttrValueVoList);
            }
        }, threadPoolExecutor);

        //9.当前sku的销售属性
        CompletableFuture<Void> skuSaleCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsGmallClient.querySaleAttrValueBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> skuAttrValueMap = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(skuAttrValueMap);
            }
        }, threadPoolExecutor);

/*        //10.当前sku所属spu下，所有的sku组合
        List<SaleAttrValueVo> saleAttrValueVos = pmsGmallClient.querySaleItemAttrValueBySpuId(skuEntity.getSpuId()).getData();
        if (!CollectionUtils.isEmpty(saleAttrValueVoList)) {
            itemVo.setSaleAttrs(saleAttrValueVos);
        }*/

        //11.商品描述
        CompletableFuture<Void> imageSkuCompletableFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> skuImagesEntities = pmsGmallClient.queryImagesBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuImagesEntities)) {
                itemVo.setImages(skuImagesEntities);
            }
        }, threadPoolExecutor);

        //13.获取spu下sku销售属性与attrId的映射关系
        CompletableFuture<Void> stringCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<String> stringResponseVo = pmsGmallClient.querySaleAttrMappingSkuIdBySpuId(skuEntity.getSpuId());
            if (stringResponseVo != null) {
                String data = stringResponseVo.getData();
                itemVo.setSkusJson(data);
            }
        }, threadPoolExecutor);

        //12.规格参数组及组下的规格参数与值
        CompletableFuture<Void> groupCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            List<ItemGroupVo> itemGroupVos = pmsGmallClient.queryGroupWithAttrValue(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId).getData();
            itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);
        CompletableFuture.allOf(categoryCompletableFuture, brandCompletableFuture, spuCompletableFuture, spuDescCompletableFuture,
                imageCompletableFuture, saleCompletableFuture, wareCompletableFuture, saleAttrCompletableFuture, skuSaleCompletableFuture,
                groupCompletableFuture, stringCompletableFuture,imageSkuCompletableFuture).join();
        return itemVo;
    }
}
