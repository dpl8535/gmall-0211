package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * spu信息
 *
 * @author Mr.Ding
 * @email MrDing@atguigu.com
 * @date 2020-07-20 19:47:01
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo getSpuByCategoryIdAndPageParamVo(Long cId, PageParamVo pageParamVo);

    void bigSave(SpuVo spuVo) throws FileNotFoundException;

    void saveSku(SpuVo spuVo, Long spuId);

    void saveBaseAttr(SpuVo spuVo, Long spuId);


    Long saveSpu(SpuVo spuVo);

}

