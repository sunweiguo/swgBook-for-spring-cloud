package com.swg.zipkinserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/12/1 13:16
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/hello")
    public String hi(){
        return "hello user!";
    }
}