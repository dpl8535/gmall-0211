package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "wms:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    //    @Transactional
    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVos, String orderToken) {

        // 1.判空
        if (CollectionUtils.isEmpty(lockVos)) {
            return null;
        }

        // 2.当传过来的数据不为空时,验库存并锁库存
        lockVos.forEach(lockVo ->
                this.checkLock(lockVo));

        // 3.判断有没有锁定失败的，即购物车中的商品可能有缺货的无法，或者库存不够的，导致无法锁定成功如果有则所有锁住的库存都应该被释放掉
        if (lockVos.stream().anyMatch(lockVo -> !lockVo.getLock())) { // 判断如果有任何一个锁定为false的则执行下面的语句
            // 4.把锁定成功的存到一个list中
            List<SkuLockVo> successLockVos = lockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(successLockVos)) {
                // 5.如果有则解锁
                successLockVos.forEach(lockVo -> {
                    this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
                });
                // 6.并返回失败的商品
                return lockVos.stream().filter(SkuLockVo -> !SkuLockVo.getLock()).collect(Collectors.toList());
            }
        }

        // 7.方便解锁库存把锁定的库存存入redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVos));

        // 当订单过期时，将订单号给库存交换机
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "stock.ttl", orderToken);
//        int i = 10 / 0;
        // 8.锁定成功返回null
        return null;
    }

    @Autowired
    private WareSkuMapper wareSkuMapper;

    /**
     * 根据skuLockVo中的参数判断是否有库存，有则锁住库存
     * 为了保证验库存和锁库存的原子性，使用分布式锁
     *
     * @param lockVo
     */
    private void checkLock(SkuLockVo lockVo) {
        RLock fairLock = redissonClient.getFairLock("lock" + lockVo.getSkuId());
        fairLock.lock();

        // 1.验库存，查询库存
        List<WareSkuEntity> skuEntities = this.wareSkuMapper.check(lockVo.getSkuId(), lockVo.getCount());
        if (CollectionUtils.isEmpty(skuEntities)) {
            lockVo.setLock(false);
            fairLock.unlock();
            return;
        }

        // 2.锁库存，简化锁第一个仓库的库存
        Long id = skuEntities.get(0).getId();
        if (this.wareSkuMapper.lock(id, lockVo.getCount()) == 1) {
            lockVo.setLock(true);
            lockVo.setWareSkuId(id);
        }
        fairLock.unlock();
    }
}