package com.swg.weathereurekaclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/11/25 14:42
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
public class HelloController {
    @Value("${auther}")
    private String auther;

    @GetMapping("/hello")
    public String hello(){
        return auther;
    }
}
