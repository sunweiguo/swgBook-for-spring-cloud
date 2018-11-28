package com.swg.springcloudeureka.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author 【swg】.
 * @Date 2018/11/27 15:24
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
public class HelloServiceImpl implements IHelloServie {

    @Autowired
    RestTemplate restTemplate;

    @Override
    @HystrixCommand(fallbackMethod = "helloFallBack")
    public String hiService(String name) throws ExecutionException, InterruptedException {
        Future<String> future = new AsyncResult<String>() {
            @Override
            public String invoke() {
                return restTemplate.getForEntity("http://SERVICE-HI/hello?name="+name,String.class).getBody();
            }
        };
        return future.get();
    }

    public String helloFallBack(String name){
        return "<font color='red'>error</font>";
    }


}