package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import jdk.nashorn.internal.ir.JoinPredecessor;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author dplStart
 * @create 下午 03:20
 * @Description
 */
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Around(value = "@annotation(com.atguigu.gmall.index.aspect.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{

        //获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        //获取到目标方法
        Method method = signature.getMethod();

        //获取到目标方法上的注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);

        //获取到注解前缀
        String prefix = gmallCache.prefix();

        //获取到目标方法的 形参
        Object[] args = joinPoint.getArgs();

        //前缀 和 形参组成缓存key
        String key =  prefix + Arrays.asList(args);

        //获取到方法返回值类型
        Class<?> returnType = method.getReturnType();

        //1.查询缓存，缓存中有 直接返回，没有则查询数据库
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)){
            return JSON.parseObject(json,returnType);
        }

        //获取到锁
        String lock = gmallCache.lock();
        //2.加入分布式锁 
        RLock fairLock = redissonClient.getFairLock(lock + args);
        fairLock.lock();

        //3.再查询缓存
        String json2 = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json2)){
            fairLock.unlock();
            return JSON.parseObject(json2,returnType);
        }

        //执行目标方法
        Object result = joinPoint.proceed(joinPoint.getArgs());

        int cacheTime = gmallCache.cache();
        int randomTime = gmallCache.random();
        redisTemplate.opsForValue().set(key,JSON.toJSONString(result), cacheTime + randomTime, TimeUnit.SECONDS);

        //解锁
        fairLock.unlock();

        return result;
    }



}
