package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author dplStart
 * @create 下午 06:14
 * @Description
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        //允许的头信息
        corsConfiguration.addAllowedHeader("*");

        //允许通过的方法 get post delete put hide
        corsConfiguration.addAllowedMethod("*");

        //允许跨域的地址信息，写*则不允许使用cookie
        corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
        corsConfiguration.addAllowedOrigin("http://gmall.com");

        //允许携带coolie信息
        corsConfiguration.setAllowCredentials(true);

        //添加映射拦截一切请求
        UrlBasedCorsConfigurationSource configurationSource =
                new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(configurationSource);
    }

}
