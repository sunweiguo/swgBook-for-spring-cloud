package com.swg.springcloudeureka.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/11/27 15:25
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
public class HelloController {
    @Value("${server.port}")
    private String port;

    @RequestMapping("hello")
    public String hello(@RequestParam("name")String name){
        return "hi "+ name +",you are from " + port;
    }

}
