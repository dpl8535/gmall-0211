package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dplStart
 * @create 上午 08:45
 * @Description
 */
public interface AuthService {
    void cerAuth(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
