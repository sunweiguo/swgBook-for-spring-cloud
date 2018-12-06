package com.stylefeng.guns.api.user;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 15:37
 * @DESC
 * @CONTACT 317758022@qq.com
 */
public interface UserAPI {

    //登陆
    int login(String username,String password);

    //注册
    boolean register(UserModel userModel);

    //检查用户名是否已经存在
    boolean checkUsername(String username);

    //返回用户信息
    UserInfoModel getUserInfo(int userId);

    //更新用户信息
    UserInfoModel updateInfo(UserInfoModel userInfoModel);
}
