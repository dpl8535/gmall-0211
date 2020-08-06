package com.atguigu.gmall.gateway.filters;

import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.config.JwtProperties;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author dplStart
 * @create 下午 06:37
 * @Description
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.PathesConfig> {

    @Autowired
    private JwtProperties jwtProperties;

    public AuthGatewayFilterFactory() {
        super(PathesConfig.class);
    }

    @Override
    public GatewayFilter apply(PathesConfig config) {
        return (exchange, chain) -> {

            // 1.判断当前路径是否在黑名单中如果不在则可以直接放行（黑名单即在yml中配置的filters信息）
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            // 获取到当前路径
            String curPath = request.getURI().getPath();
            // 通过config获取到黑名单中的路径
            List<String> pathes = config.getPathes();
            if (pathes.stream().allMatch(path -> curPath.indexOf(path) == -1)) {
                return chain.filter(exchange);
            }

            // 2.名单中如果有该该路径 则判断是否有token
            String token = request.getHeaders().getFirst("token");
            if (StringUtils.isBlank(token)) {
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if (!CollectionUtils.isEmpty(cookies) && cookies.containsKey(this.jwtProperties.getCookieName())) {
                    token = cookies.getFirst(this.jwtProperties.getCookieName()).getValue();
                }
            }

            // 3.如果token 为空则拦截跳转到登陆页面
            if (StringUtils.isBlank(token)) {
                return interceptor(request, response);
            }

            try {
                // 4.解析token类型获取到用户的信息
                Map<String, Object> map = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

                // 5.判断当前用户是否和token中的用户信息一致，使用ip进行判断
                String ip = map.get("ip").toString();
                String curIp = IpUtil.getIpAddressAtGateway(request);
                if (!StringUtils.equals(ip, curIp)) {
                    // 不一致说明被盗用，直接拦截
                    return interceptor(request, response);
                }
                // 6.如果一致则把信息传递到后方
                String userId = map.get("id").toString();
                String username = map.get("username").toString();
                request = request.mutate().header("userId", userId).header("username", username).build();
                exchange = exchange.mutate().request(request).build();
                System.out.println("自定义过滤器" + config);
            } catch (Exception e) {
                e.printStackTrace();
                return interceptor(request, response);
            }
            // 7.放行
            return chain.filter(exchange);
        };
    }

    private Mono<Void> interceptor(ServerHttpRequest request, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin?returnUrl=" + request.getURI());
        return response.setComplete();
    }

    /**
     * 指定字段顺序
     * 可以通过不同字段分别读取路径名
     *
     * @return
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("pathes");
    }

    /**
     * 指定读取字段的结果集类型
     * 默认通过map方式
     *
     * @return
     */
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Data
    @ToString
    public static class PathesConfig {
        private List<String> pathes;
    }
}
