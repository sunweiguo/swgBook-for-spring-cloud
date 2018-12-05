package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import org.springframework.stereotype.Component;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 19:21
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Component
public class UserClient {

    @Reference
    private UserAPI userAPI;

    public String login(String username, String password) {
        return userAPI.login(username,password);
    }
}
