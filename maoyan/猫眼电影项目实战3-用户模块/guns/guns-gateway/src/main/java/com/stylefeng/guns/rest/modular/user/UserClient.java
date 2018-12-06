package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.UserInfoModel;
import com.stylefeng.guns.api.user.UserModel;
import com.stylefeng.guns.rest.common.CurrentUser;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 19:21
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@RestController
@RequestMapping("/user/")
public class UserClient {

    @Reference(interfaceClass = UserAPI.class)
    private UserAPI userAPI;

    @PostMapping("/register")
    public ResponseVO register(UserModel userModel){
        //验证用户名不能为空
        if(userModel.getUsername() == null || userModel.getUsername().trim().length() == 0){
            return ResponseVO.serviceFail("用户名不能为空");
        }
        //验证密码不能为空
        if(userModel.getPassword() == null || userModel.getPassword().trim().length() == 0){
            return ResponseVO.serviceFail("密码不能为空");
        }
        boolean isSuccess = userAPI.register(userModel);
        if(isSuccess){
            return ResponseVO.success("注册成功");
        }
        return ResponseVO.serviceFail("注册失败");
    }

    @PostMapping("/check")
    public ResponseVO checkUsername(String username) {
        //验证用户名不能为空
        if(username == null || username.trim().length() == 0){
            return ResponseVO.serviceFail("用户名不能为空");
        }

        boolean isExist = userAPI.checkUsername(username);
        if(isExist){
            //为true的时候，说明用户名不存在，则用户名可用
            return ResponseVO.success("用户名不存在");
        }else{
            return ResponseVO.serviceFail("用户名已存在");
        }
    }

    @GetMapping("/logout")
    public ResponseVO logout(){
        //前端存储JWT：七天
        //后端redis中会存储活动用户信息：30min
        //然后根据JWT里面的userid去redis中查询是否为活跃用户，如果不是，则要求重新登陆

        //所以，针对上面这种情况的话，首先前端要删除JWT
        //redis中也要删除活跃用户缓存

        //现状：没有redis，前端直接删除JWT
        return ResponseVO.success("用户退出成功");

    }

    @GetMapping("/getUserInfo")
    public ResponseVO getUserInfo(){
        //获取当前用户
        String userId = CurrentUser.getUserId();
        if(userId != null || userId.trim().length() > 0){
            //到后端查询
            UserInfoModel userInfoModel = userAPI.getUserInfo(Integer.parseInt(userId));
            if(userInfoModel != null){
                return ResponseVO.success(userInfoModel);
            }else {
                return ResponseVO.serviceFail("查询用户信息失败");
            }
        }
        return ResponseVO.serviceFail("用户未登陆");
    }

    @PostMapping("/updateUserInfo")
    public ResponseVO getUserInfo(UserInfoModel userInfoModel){
        //获取当前用户
        String userId = CurrentUser.getUserId();
        if(userId != null || userId.trim().length() > 0){
            //到后端查询
            if(userInfoModel.getUuid() == Integer.parseInt(userId)){
                UserInfoModel getFromDB = userAPI.getUserInfo(Integer.parseInt(userId));
                userInfoModel.setBeiginTime(getFromDB.getBeiginTime());
                UserInfoModel result = userAPI.updateInfo(userInfoModel);
                if(result != null){
                    return ResponseVO.success(userInfoModel);
                }else {
                    return ResponseVO.serviceFail("更新用户信息失败");
                }
            }else{
                return ResponseVO.serviceFail("请修改您个人自己的信息！");
            }
        }
        return ResponseVO.serviceFail("用户未登陆");
    }


}
