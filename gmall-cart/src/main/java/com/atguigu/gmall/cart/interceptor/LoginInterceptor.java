package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author dplStart
 * @create 下午 11:34
 * @Description
 */
@Component
@EnableConfigurationProperties({JwtProperties.class})
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 声明线程的局部变量
     */
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal();

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 前置方法
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1.获取userKey
        String userKeyName = jwtProperties.getUserKeyName();
        String userKey = CookieUtils.getCookieValue(request, userKeyName);
        // 2.如果为空就创建 一个userKey
        if (StringUtils.isEmpty(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, jwtProperties.getUserKeyName(), userKey, 180 * 24 * 3600);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);


        // 3.获取到登录信息
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isNotEmpty(token)) {
            // 4.解析jwt
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            Long userId = Long.valueOf(map.get("id").toString());
            userInfo.setUserId(userId);
        }

        THREAD_LOCAL.set(userInfo);
        return true;
    }

    /**
     * 声明静态公共方法获取到userInfo信息
     *
     * @return
     */
    public static UserInfo getThreadLocal() {
        return THREAD_LOCAL.get();
    }

    /**
     * 由于使用的是线程池中的线程，所以要释放线程资源
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}

