package com.stylefeng.guns.api.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 21:27
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Data
public class UserModel implements Serializable {

    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
}
