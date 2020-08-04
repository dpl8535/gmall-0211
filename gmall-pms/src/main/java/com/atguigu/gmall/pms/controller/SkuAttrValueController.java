package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku销售属性&值
 *
 * @author Mr.Ding
 * @email MrDing@atguigu.com
 * @date 2020-07-20 19:47:01
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @GetMapping("sku/sku/{spuId}")
    public ResponseVo<String> querySaleAttrMappingSkuIdBySpuId(@PathVariable("spuId") Long spuId){
        String s = this.skuAttrValueService.querySaleAttrMappingSkuIdBySpuId(spuId);
        return ResponseVo.ok(s);
    }

    /**
     * 根据skuId获取到当前sku的属性信息
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValueBySkuId(@PathVariable("skuId") Long skuId){
        List<SkuAttrValueEntity> attrValueEntities = this.skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return ResponseVo.ok(attrValueEntities);
    }

    /**
     * 根据spuId获取到销售属性信息
     * @param spuId
     * @return
     */
    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleItemAttrValueBySpuId(@PathVariable("spuId") Long spuId) {
        List<SaleAttrValueVo> saleAttrValueVos = this.skuAttrValueService.querySaleItemAttrValueBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }


    /**
     * 根据skuId和分类id获取到规格参数及值
     *
     * @param skuId
     * @return
     */
    @GetMapping("attrVal/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrValueBySkuId(@PathVariable("skuId") Long skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySkuAttrValueBySkuId(skuId);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id) {
        SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue) {
        skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
