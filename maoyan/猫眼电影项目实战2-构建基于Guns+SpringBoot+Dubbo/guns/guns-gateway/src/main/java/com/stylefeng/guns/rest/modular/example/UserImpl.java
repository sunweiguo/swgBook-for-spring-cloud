package com.stylefeng.guns.rest.modular.example;

import com.stylefeng.guns.api.user.UserAPI;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 15:38
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public class UserImpl implements UserAPI {
    @Override
    public boolean checkUser(String username, String password) {
        return false;
    }
}
