package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:47
 * @Description
 */
@Data
public class SpuVo extends SpuEntity {

    List<String> spuImages;

    List<BaseAttrValueVo> baseAttrs;

    List<SkuVo> skus;

}
