package com.stylefeng.guns.rest.common;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 21:49
 * @DESC 用户信息放入本地线程
 * @CONTACT 317758022@qq.com
 */
public class CurrentUser {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static void saveUserId(String userId){
        threadLocal.set(userId);
    }

    public static String getUserId(){
        return threadLocal.get();
    }
}
