package com.swg.springcloudeureka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    public String hiService(String name) {
        return restTemplate.getForObject("http://SERVICE-HI/hello?name="+name,String.class);
    }


}