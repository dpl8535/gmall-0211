package com.atguigu.gmall.message.service;

import com.aliyuncs.exceptions.ClientException;

/**
 * @author dplStart
 * @create 下午 07:55
 * @Description
 */
public interface MessageService {
    void send(String mobile, String checkCode) throws ClientException;
}
