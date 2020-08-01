package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.PmsGmallClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.DistributeLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;
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

    private static final String KEY_PREFIX = "index:category:";

    @Autowired
    private DistributeLock distributeLock;

    @Override
    public List<CategoryEntity> queryLevel1Category() {
        ResponseVo<List<CategoryEntity>> categoriesByParentIdVo = pmsGmallClient.getCategoriesByParentId(0l);
        return categoriesByParentIdVo.getData();
    }

    @Override
    public List<CategoryEntity> queryLevel2And3Category(Long pid) {

        //1.首先判断redis 中是否有数据,如果有直接返回
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return categoryEntities;
        }

        //2.如果没有则在数据库中查询
        ResponseVo<List<CategoryEntity>> categoriesWitSubs = pmsGmallClient.getCategoriesWitSubs(pid);
        List<CategoryEntity> categoryEntities = categoriesWitSubs.getData();
        //3.把从数据库中查询到的数据存入到缓存中
        redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 30, TimeUnit.DAYS);

        return categoryEntities;
    }

    /**
     * synchronized 单jvm有效
     */
    @Override
    public void testLock() {
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

        this.testSubLock("lock", uuid);

        this.distributeLock.unLock("lock", uuid);
    }

    private void testSubLock(String lockName, String uuid) {

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
