package com.swg.springcloudstreamproducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/11/30 14:10
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
public class HelloController {

    @Autowired
    private SendService sendService;


    @RequestMapping("/send/{msg}")
    public void send(@PathVariable("msg") String msg){
        System.out.println("发送了。。。"+msg);
        sendService.sendMsg(msg);
    }


}
