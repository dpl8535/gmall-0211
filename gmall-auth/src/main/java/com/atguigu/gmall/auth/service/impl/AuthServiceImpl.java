package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.UmsGmallClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dplStart
 * @create 上午 08:46
 * @Description
 */
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UmsGmallClient umsGmallClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void cerAuth(String loginName,
                        String password,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
        // 1.判断用户是否存在
        ResponseVo<UserEntity> userEntityResponseVo = umsGmallClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        // 2.判空，抛异常
        if (userEntity == null){
            throw new UserException("用户名或密码不正确");
        }

        // 3.获取到用户id 和 用户名存入到map中，
        Map<String,Object> map = new HashMap<>();
        map.put("id", userEntity.getId());
        map.put("username", loginName);
        // 保证jwt安全性，把ip放到map中
        map.put("ip", IpUtil.getIpAddressAtService(request));

        // 4.生成jwt
        String jwt = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

        // 5.把jwt存到cookie中
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),jwt,jwtProperties.getExpire() * 60);

        // 6.将用户名存入到cookie中
        CookieUtils.setCookie(request,response,jwtProperties.getUnick(),userEntity.getNickname(),jwtProperties.getExpire() * 60);

    }
}
