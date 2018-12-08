package com.swg.springcloudeureka.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.swg.springcloudeureka.Book;
import com.swg.springcloudeureka.service.BookService;
import com.swg.springcloudeureka.service.IHelloServie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author 【swg】.
 * @Date 2018/11/27 15:25
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
public class HelloController {

    @Autowired
    private IHelloServie helloServie;
    @Autowired
    private BookService bookService;

    @RequestMapping("hello")
    public String hello(@RequestParam("name")String name) throws ExecutionException, InterruptedException {
        return helloServie.hiService(name);
    }

    @RequestMapping("/books")
    public void test7() throws ExecutionException, InterruptedException {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        Future<Book> f1 = bookService.test10(1l);
        Future<Book> f2 = bookService.test10(2l);
        Future<Book> f3 = bookService.test10(3l);
        Book b1 = f1.get();
        Book b2 = f2.get();
        Book b3 = f3.get();
        Thread.sleep(3000);
        Future<Book> f4 = bookService.test10(4l);
        Book b4 = f4.get();
        System.out.println("b1>>>"+b1);
        System.out.println("b2>>>"+b2);
        System.out.println("b3>>>"+b3);
        System.out.println("b4>>>"+b4);
        context.close();
    }

}
