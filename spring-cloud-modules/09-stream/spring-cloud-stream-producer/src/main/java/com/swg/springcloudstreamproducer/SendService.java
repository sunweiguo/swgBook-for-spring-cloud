package com.swg.springcloudstreamproducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @Author 【swg】.
 * @Date 2018/11/30 14:12
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@EnableBinding(Source.class)
public class SendService {

    @Autowired
    private Source source;


    public void sendMsg(String msg){
        source.output().send(MessageBuilder.withPayload(msg).build());
    }

}