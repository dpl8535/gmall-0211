package com.atguigu.gmall.message.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.gmall.message.service.MessageService;
import com.atguigu.gmall.message.utils.MessageProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author dplStart
 * @create 下午 07:55
 * @Description
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageProperties messageProperties;

    @Override
    public void send(String phone, String random) throws ClientException {
        String keyId = messageProperties.getKeyId();
        String keySecret = messageProperties.getKeySecret();
        String regionId = messageProperties.getRegionId();
        String signName = messageProperties.getSignName();
        String templateCode = messageProperties.getTemplateCode();

        DefaultProfile profile = DefaultProfile.getProfile(regionId, keyId, keySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("regionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);

        HashMap<String, Object> param = new HashMap<>();
        param.put("code", random);
        Gson gson = new Gson();
        String checkNumber = gson.toJson(param);

        request.putQueryParameter("TemplateParam", checkNumber);
        CommonResponse response = client.getCommonResponse(request);
        String data = response.getData();

        //解析响应结果
        HashMap<String,String> map = gson.fromJson(data, HashMap.class);
        String code = map.get("code");
        String message = map.get("Message");
        System.out.println(data);
        log.info("响应码为:", code);
        log.info("响应信息为:", message);

    }
}
