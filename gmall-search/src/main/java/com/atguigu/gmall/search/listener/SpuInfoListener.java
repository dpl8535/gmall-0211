package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.PmsGmallClient;
import com.atguigu.gmall.search.feign.WmsGmallClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.ws.BindingType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dplStart
 * @create 下午 05:12
 * @Description
 */
@Component
public class SpuInfoListener {

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private WmsGmallClient wmsGmallClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "PMS-SAVE-QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS-ITEM-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsGmallClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();

        ResponseVo<List<SkuEntity>> skuResponseVo = pmsGmallClient.getSkuBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {

            //遍历skuEntities把获取到的数据映射到goodsList中，goodsRepository.saveAll存入到es中
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();

                //把skuEntity信息存到goods中
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setImage(skuEntity.getDefaultImage());

                //商品商家时间
                goods.setCreateTime(spuEntity.getCreateTime());

                //根据skuId获取到品牌信息存储到goods中
                ResponseVo<BrandEntity> brandEntityResponseVo = pmsGmallClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();
                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }

                //根据skuId获取到分类信息存储到goods中
                ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsGmallClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                //根据spuId获取到基本属性
                ResponseVo<List<SpuAttrValueEntity>> spuAttrValueVo = pmsGmallClient.querySpuAttrValueBySpuId(spuEntity.getId());
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueVo.getData();
                List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    //创建一个空的link用来存储sku和spu的属性信息，然后存入到goods.setSearchAttrValue中
                    List<SearchAttrValue> spuSearchAttrValueList = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        searchAttrValue.setAttrId(spuAttrValueEntity.getAttrId());
                        searchAttrValue.setAttrName(spuAttrValueEntity.getAttrName());
                        searchAttrValue.setAttrValue(spuAttrValueEntity.getAttrValue());
                        return searchAttrValue;
                    }).collect(Collectors.toList());
                    searchAttrValues.addAll(spuSearchAttrValueList);
                    //goods.setSearchAttrs(spuSearchAttrValueList);
                }

                //根据skuId获取到销售属性
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValueVo = pmsGmallClient.querySkuAttrValueBySkuId(skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueVo.getData();
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    List<SearchAttrValue> skuSearchAttrValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValue searchAttrValue = new SearchAttrValue();
                        searchAttrValue.setAttrId(skuAttrValueEntity.getAttrId());
                        searchAttrValue.setAttrValue(skuAttrValueEntity.getAttrValue());
                        searchAttrValue.setAttrName(skuAttrValueEntity.getAttrName());
                        return searchAttrValue;
                    }).collect(Collectors.toList());
                    searchAttrValues.addAll(skuSearchAttrValues);
                    goods.setSearchAttrs(searchAttrValues);
                }

                //根据skuId获取到库存信息
                ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsGmallClient.getWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get().intValue());
                }
                return goods;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
        }

        try {
            //手动确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()) {
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            }
        }

    }

}
