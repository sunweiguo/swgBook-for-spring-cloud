package com.swg.springboot.consumer.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.swg.springboot.provider.service.SendService;
import org.springframework.stereotype.Component;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 10:19
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Component
public class HelloConsumer {

    @Reference(url = "dubbo://localhost:20880")
    SendService sendService;


    public void senMsg(String message){
        System.out.println(sendService.sendMsg(message));
    }

}
