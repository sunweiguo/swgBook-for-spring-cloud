package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.stylefeng.guns.api.UserAPI;
import org.springframework.stereotype.Component;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 19:21
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Component
@Service(interfaceClass = UserAPI.class)
public class UserServiceImpl implements UserAPI{
    @Override
    public String login(String username, String password) {
        return "=====username="+username+",password="+password;
    }
}
