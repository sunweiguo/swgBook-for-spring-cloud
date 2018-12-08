package com.swg.springcloudeureka.controller;

import com.swg.springcloudeureka.service.HelloServiceCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    public String hello() throws ExecutionException, InterruptedException {
        HelloServiceCommand command = new HelloServiceCommand("hello",restTemplate);
        long now = System.currentTimeMillis();
        Future<String> future = command.queue();
        System.out.println("start");
        long end = System.currentTimeMillis();
        System.out.println(end - now);
        String res = future.get();
        long last = System.currentTimeMillis()-end;
        System.out.println(last);
        return res;
    }

}
