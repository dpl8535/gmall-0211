package com.atguigu.gmall.message.controller;

import com.aliyuncs.exceptions.ClientException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.RandomUtils;
import com.atguigu.gmall.message.service.MessageService;
import com.baomidou.mybatisplus.extension.api.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author dplStart
 * @create 下午 07:54
 * @Description
 */
@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @GetMapping("send/{phone}")
    public ResponseVo getCode(@PathVariable String phone) throws ClientException {

        //生成验证码
        String checkCode = RandomUtils.getFourBitRandom();
        //发送验证码
        messageService.send(phone, checkCode);
        //将验证码存入redis缓存
        redisTemplate.opsForValue().set(phone, checkCode, 5, TimeUnit.MINUTES);

        return ResponseVo.ok();
    }
}
