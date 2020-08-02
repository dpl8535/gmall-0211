package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

/**
 * @author dplStart
 * @create 下午 03:12
 * @Description
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存过期时间 单位分钟
     * @return
     */
    int cache() default 5;

    /**
     * 防止缓存雪崩设置的随机值
     * @return
     */
    int random() default 5;

    /**
     * 防止击穿，增加分布式锁
     * @return
     */
    String lock() default "lock";

}
