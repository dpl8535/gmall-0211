package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * @author dplStart
 * @create 下午 10:20
 * @Description
 */
public interface IndexService {
    List<CategoryEntity> queryLevel1Category();

    List<CategoryEntity> queryLevel2And3Category(Long pid);

    void testLock();

    void testLock2();

    public void testSubLock2();

    public void testLock3();
}
