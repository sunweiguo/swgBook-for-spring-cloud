package com.swg.springcloudconfigclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/11/29 10:19
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
@RefreshScope
public class TestController {

    @Value("${name}")
    private String name;

    @RequestMapping("/test")
    public String test(){
        return "hello."+this.name;
    }

}
