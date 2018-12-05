package com.stylefeng.guns.rest.modular.example;

import com.stylefeng.guns.rest.modular.user.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 20:16
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserClient userClient;

    @RequestMapping("login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password){
        return userClient.login(username,password);
    }
}
