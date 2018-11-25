package com.swg.weathereurekaclient.controller;

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
@RequestMapping("hello")
public class HelloController {
    @GetMapping
    public String hello(){
        return "hello zuul";
    }
}
