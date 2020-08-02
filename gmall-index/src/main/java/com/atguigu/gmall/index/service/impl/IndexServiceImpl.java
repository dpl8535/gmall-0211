package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.config.RedissonConfig;
import com.atguigu.gmall.index.feign.PmsGmallClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.DistributeLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.*;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dplStart
 * @create 下午 10:20
 * @Description
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private PmsGmallClient pmsGmallClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributeLock distributeLock;

    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:category:";

    @Override
    public List<CategoryEntity> queryLevel1Category() {
        ResponseVo<List<CategoryEntity>> categoriesByParentIdVo = pmsGmallClient.getCategoriesByParentId(0l);
        return categoriesByParentIdVo.getData();
    }

    @Override
    @GmallCache(prefix = "index:category:",cache = 43200,random = 720,lock = "lock")
    public List<CategoryEntity> queryLevel2And3Category(Long pid) {

        //2.如果没有则在数据库中查询
        ResponseVo<List<CategoryEntity>> categoriesWitSubs = pmsGmallClient.getCategoriesWitSubs(pid);
        List<CategoryEntity> categoryEntities = categoriesWitSubs.getData();


        return categoryEntities;
    }


    public List<CategoryEntity> queryLevel2And3Category2(Long pid) {
        //1.首先判断redis 中是否有数据,如果有直接返回
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {

            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return categoryEntities;
        }

        //防止缓存击穿加分布式锁
        RLock lock = redissonClient.getFairLock("lock");
        lock.lock();

        //然后再查询缓存
        String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json2)) {
            lock.unlock();
            List<CategoryEntity> categoryEntities = JSON.parseArray(json2, CategoryEntity.class);
            return categoryEntities;
        }

        //2.如果没有则在数据库中查询
        ResponseVo<List<CategoryEntity>> categoriesWitSubs = pmsGmallClient.getCategoriesWitSubs(pid);
        List<CategoryEntity> categoryEntities = categoriesWitSubs.getData();
        //3.把从数据库中查询到的数据存入到缓存中，防止穿透把查询到的空值也存到缓存中，防止雪崩给过期时间加一个随机值
        redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 30 + new Random().nextInt(5), TimeUnit.DAYS);
        lock.unlock();

        return categoryEntities;
    }


    @Override
    public String testWrite() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
//        rwLock.writeLock().lock();
        rwLock.writeLock().lock(10, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("msg", UUID.randomUUID().toString());
        System.out.println("数据写入成功！");
//        rwLock.writeLock().unlock();
        return "数据写入成功";
    }

    @Override
    public String testRead() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
//        rwLock.readLock().lock();
        rwLock.readLock().lock(10, TimeUnit.SECONDS);
        String msg = redisTemplate.opsForValue().get("msg");
        System.out.println("读到的数据 msg = " + msg);
//        rwLock.readLock().unlock();
        return msg;
    }

    @Override
    public String testSemaphore() {
        String acquire = null;
        try {
            RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore("testSemaphore");
            acquire = semaphore.acquire();
            semaphore.release(acquire);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return acquire;
    }

    @Override
    public String testLatch() {
        try {
            RCountDownLatch countDown = redissonClient.getCountDownLatch("countDown");
            countDown.trySetCount(6);
            countDown.await();
            return "关门了";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String testOut() {
        RCountDownLatch countDown = redissonClient.getCountDownLatch("countDown");
        countDown.countDown();

        return "出来了一个人";
    }

    /**
     * synchronized 单jvm有效
     */
    @Override
    public void testLock() {

        RLock lock = null;
        try {
            lock = redissonClient.getLock("lock");
            lock.lock(50, TimeUnit.SECONDS); //50s后自动释放锁
            String numString = this.redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(numString)) {
                return;
            }

            Integer num = Integer.parseInt(numString);
            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

            this.testSubLock();
        } finally {

            lock.unlock();
        }

    }

    private void testSubLock() {

        RLock lock = null;
        try {
            lock = redissonClient.getLock("lock");
            lock.lock(50, TimeUnit.SECONDS);

            System.out.println("这是一个子方法，也需要获取锁。。。。。。");
        } finally {
            lock.unlock();

        }


    }

    public void testLock4() {
        String uuid = UUID.randomUUID().toString();
        this.distributeLock.tryLock("lock", uuid, 30l);
        String numString = this.redisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(numString)) {
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Integer num = Integer.parseInt(numString);
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));

        this.testSubLock4("lock", uuid);

        this.distributeLock.unLock("lock", uuid);
    }

    private void testSubLock4(String lockName, String uuid) {

        this.distributeLock.tryLock(lockName, uuid, 30l);

        System.out.println("这是一个子方法，也需要获取锁。。。。。。");

        this.distributeLock.unLock(lockName, uuid);
    }

    @Override
    public void testLock3() {
        //加分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (!lock) {
            try {
                Thread.sleep(100);
                testLock3();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(num)) {
                return;
            }

            //对资源进行操作
            int i = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num", String.valueOf(++i));

            testSubLock3();

            // 释放锁 lua脚本保证删除的原子性
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
        }
    }

    public void testSubLock3() {
        //加分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        if (!lock) {
            try {
                Thread.sleep(100);
                testSubLock3();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("执行子方法！");

            // 释放锁 lua脚本保证删除的原子性
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
        }
    }

    @Override
    public void testLock2() {
        //加分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        if (!lock) {
            try {
                Thread.sleep(100);
                testLock2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(num)) {
                return;
            }

            //对资源进行操作
            int i = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num", String.valueOf(++i));

            // 释放锁 lua脚本保证删除的原子性
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);

            /*if (StringUtils.equals(uuid, redisTemplate.opsForValue().get("lock"))) {
                redisTemplate.delete("lock");
            }*/
        }
    }

    public void testSubLock2() {
        //加分布式锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);

        if (!lock) {
            try {
                Thread.sleep(100);
                testSubLock2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String num = redisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(num)) {
                return;
            }

            //对资源进行操作
            int i = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num", String.valueOf(++i));

            // 释放锁 lua脚本保证删除的原子性
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
        }
    }
}
