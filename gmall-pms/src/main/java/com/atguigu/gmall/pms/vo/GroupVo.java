package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 06:08
 * @Description
 */
@Data
public class GroupVo extends AttrGroupEntity {

    private List<AttrEntity> attrEntities;

}
