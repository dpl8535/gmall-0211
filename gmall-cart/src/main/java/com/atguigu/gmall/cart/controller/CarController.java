package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.UserInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author dplStart
 * @create 下午 11:37
 * @Description
 */
@Controller
public class CarController {

    @GetMapping("test")
    @ResponseBody
    public String test(){

        UserInfo userInfo = LoginInterceptor.getThreadLocal();
        System.out.println(userInfo);
        return "hello cart!";
    }
}
