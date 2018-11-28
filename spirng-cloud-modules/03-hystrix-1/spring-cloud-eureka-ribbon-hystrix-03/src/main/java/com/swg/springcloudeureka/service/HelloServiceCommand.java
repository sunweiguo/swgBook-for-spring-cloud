package com.swg.springcloudeureka.service;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.web.client.RestTemplate;

/**
 * @Author 【swg】.
 * @Date 2018/11/28 10:35
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public class HelloServiceCommand extends HystrixCommand<String> {

    private RestTemplate restTemplate;

    public HelloServiceCommand(String commandGroupKey,RestTemplate restTemplate) {
        super(HystrixCommandGroupKey.Factory.asKey(commandGroupKey));
        this.restTemplate = restTemplate;
    }

    @Override
    protected String run() throws Exception {
        return restTemplate.getForEntity("http://SERVICE-HI/hello?name='swg'",String.class).getBody();
    }

    @Override
    protected String getFallback() {
        return "<font color='red'>error</font>";
    }
}
