package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 07:46
 * @Description
 */
@Data
public class ItemGroupVo {

    private Long groupId;
    private String groupName;
    private List<AttrValueVo> attrs;
}
