package com.swg.springcloudeureka.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.swg.springcloudeureka.service.IHelloServie;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IHelloServie helloServie;

    @RequestMapping("hello")
    public String hello(@RequestParam("name")String name){
        return helloServie.hiService(name);
    }

}
