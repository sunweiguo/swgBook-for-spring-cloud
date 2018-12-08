package com.swg.springcloudeureka.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.swg.springcloudeureka.Book;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @Author 【swg】.
 * @Date 2018/11/28 14:58
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Service
public class BookService {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCollapser(batchMethod = "test11",collapserProperties = {@HystrixProperty(name ="timerDelayInMilliseconds",value = "100")})
    public Future<Book> test10(Long id) {
        return null;
    }

    @HystrixCommand
    public List<Book> test11(List<Long> ids) {
        System.out.println("test9---------"+ids+"Thread.currentThread().getName():" + Thread.currentThread().getName());
        Book[] books = restTemplate.getForObject("http://SERVICE-HI/getbooks?ids={1}", Book[].class, StringUtils.join(ids, ","));
        return Arrays.asList(books);
    }

}
