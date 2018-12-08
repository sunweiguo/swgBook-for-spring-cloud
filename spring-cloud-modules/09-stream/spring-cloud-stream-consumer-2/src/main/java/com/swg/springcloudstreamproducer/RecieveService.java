package com.swg.springcloudstreamproducer;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @Author 【swg】.
 * @Date 2018/11/30 14:18
 * @DESC
 * @CONTACT 317758022@qq.com
 */
//消息接受端，stream给我们提供了Sink,Sink源码里面是绑定input的，要跟我们配置文件的imput关联的。
@EnableBinding(Sink.class)
public class RecieveService {

    @StreamListener(Sink.INPUT)
    public void recieve(Object payload){
        System.out.println("====="+payload);
    }

}