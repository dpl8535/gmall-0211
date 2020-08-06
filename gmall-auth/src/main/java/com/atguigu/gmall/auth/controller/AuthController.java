package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dplStart
 * @create 下午 11:48
 * @Description
 */
@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("toLogin")
    public String toLogin(@RequestParam(value = "returnUrl", required = false) String returnUrl, Model model) {
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(@RequestParam(value = "returnUrl", required = false) String returnUrl,
                        @RequestParam("loginName") String loginName,
                        @RequestParam("password") String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception {

        authService.cerAuth(loginName, password, request, response);
        if (StringUtils.isNotBlank(returnUrl)) {
            return "redirect:" + returnUrl;
        }
        return "redirect:http://www.gmall.com";
    }


}
