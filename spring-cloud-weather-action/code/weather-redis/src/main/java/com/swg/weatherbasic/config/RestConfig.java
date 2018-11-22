package com.swg.weatherbasic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author 【swg】.
 * @Date 2018/11/19 15:02
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Configuration
public class RestConfig {
    @Autowired
    private RestTemplateBuilder builder;

    @Bean
    public RestTemplate restTemplate(){
        return builder.build();
    }
}
