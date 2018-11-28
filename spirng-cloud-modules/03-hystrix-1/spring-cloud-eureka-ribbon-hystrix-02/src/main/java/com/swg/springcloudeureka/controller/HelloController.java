package com.swg.springcloudeureka.controller;

import com.swg.springcloudeureka.service.HelloServiceCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author 【swg】.
 * @Date 2018/11/27 15:25
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
public class HelloController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("hello")
    public String hello(){
        HelloServiceCommand command = new HelloServiceCommand("hello",restTemplate);
        String res = command.execute();
        return res;
    }

}
