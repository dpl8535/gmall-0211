package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;

import jdk.internal.util.xml.impl.Input;
import lombok.Data;

/**
 * spu信息介绍
 * 
 * @author Mr.Ding
 * @email MrDing@atguigu.com
 * @date 2020-07-20 19:47:01
 */
@Data
@TableName("pms_spu_desc")
public class SpuDescEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;
	/**
	 * 商品介绍
	 */
	private String decript;

}
