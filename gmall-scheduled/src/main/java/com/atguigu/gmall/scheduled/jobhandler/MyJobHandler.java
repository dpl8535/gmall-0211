package com.atguigu.gmall.scheduled.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * @author dplStart
 * @create 下午 01:49
 * @Description
 */
@Component
public class MyJobHandler {

    @XxlJob("myJobHandler")
    public ReturnT<String> executor(String param) {

        XxlJobLogger.log("使用XxlJobLogger打印执行日志，O(∩_∩)O");
        System.out.println("我的线程执行了：" + param + "，线程：" + Thread.currentThread().getName());
        return ReturnT.SUCCESS;
    }

}
