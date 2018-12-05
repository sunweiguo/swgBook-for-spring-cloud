package com.swg.springboot.provider.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 10:08
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Component
@Service(interfaceClass = SendService.class)
public class SendSericeImpl implements SendService{
    @Override
    public String sendMsg(String message) {
        System.out.println("开始消费");
        return "hello world  "+message;
    }
}
