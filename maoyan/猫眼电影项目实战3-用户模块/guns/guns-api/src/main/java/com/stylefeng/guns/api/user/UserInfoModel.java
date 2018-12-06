package com.stylefeng.guns.api.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 21:31
 * @DESC 显示给前端的用户信息
 * @CONTACT 317758022@qq.com
 */
@Data
public class UserInfoModel implements Serializable {
    private Integer uuid;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private int sex;
    private String birthday;
    private String lifeState;
    private String biography;
    private String address;
    private String headAddress;
    private long beiginTime;
    private long updateTime;
}
