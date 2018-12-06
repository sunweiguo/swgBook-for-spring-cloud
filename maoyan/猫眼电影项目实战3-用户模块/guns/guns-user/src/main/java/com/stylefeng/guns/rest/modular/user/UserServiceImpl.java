package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.UserInfoModel;
import com.stylefeng.guns.api.user.UserModel;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.MoocUserTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocUserT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author 【swg】.
 * @Date 2018/12/5 19:21
 * @DESC
 * @CONTACT 317758022@qq.com
 */
@Component
@Service(interfaceClass = UserAPI.class)
public class UserServiceImpl implements UserAPI{

    @Autowired
    private MoocUserTMapper moocUserTMapper;

    /**
     *
     * @param username
     * @param password
     * @return 登陆成功，返回用户id，否则返回0
     */
    public int login(String username, String password) {
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUserName(username);
        MoocUserT result = moocUserTMapper.selectOne(moocUserT);
        if(result != null && result.getUuid() > 0){
            String md5PassWd = MD5Util.encrypt(password);
            if(result.getUserPwd().equals(md5PassWd)){
                return result.getUuid();
            }
        }
        return 0;
    }

    /**
     * 用户注册
     * @param userModel
     * @return 注册成功 返回true
     */
    @Override
    public boolean register(UserModel userModel) {
        MoocUserT moocUserT = new MoocUserT();

        //将注册信息转换为数据实体
        moocUserT.setUserName(userModel.getUsername());
        moocUserT.setUserPwd(MD5Util.encrypt(userModel.getPassword()));//加密
        moocUserT.setEmail(userModel.getEmail());
        moocUserT.setAddress(userModel.getAddress());
        moocUserT.setUserPhone(userModel.getPhone());

        //存进数据库
        Integer insertResult = moocUserTMapper.insert(moocUserT);
        if(insertResult > 0){
            return true;
        }
        return false;
    }

    /**
     * 检查用户名是否已经存在，存在返回false，否则返回true
     * @param username
     * @return
     */
    @Override
    public boolean checkUsername(String username) {
        EntityWrapper<MoocUserT> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("user_name",username);
        Integer result = moocUserTMapper.selectCount(entityWrapper);
        if(result != null && result == 0){
            return true;
        }
        return false;
    }

    /**
     * 获取用户信息
     * @param userId
     * @return 获取成功返回用户模型数据，否则返回空
     */
    @Override
    public UserInfoModel getUserInfo(int userId) {
        MoocUserT moocUserT =  moocUserTMapper.selectById(userId);
        //不为空的时候才能转换
        if(moocUserT != null){
            return do2UserInfo(moocUserT);
        }
        return null;
    }

    /**
     * 更新用户信息
     * @param userInfoModel
     * @return 更新成功返回新的数据，否则返回null提示更新失败
     */
    @Override
    public UserInfoModel updateInfo(UserInfoModel userInfoModel) {
        MoocUserT moocUserT = UserInfo2do(userInfoModel);
        Integer isSuccess = moocUserTMapper.updateById(moocUserT);
        if(isSuccess != null && isSuccess > 0){
            UserInfoModel userInfo = getUserInfo(moocUserT.getUuid());
            return userInfo;
        }else {
            return null;
        }
    }

    /**
     * 时间戳转换为Date类型
     * @param time
     * @return
     */
    private Date time2Date(long time){
        Date date = new Date(time);
        return date;
    }

    /**
     * 持久层类型 转为 用户模型数据
     * @param moocUserT
     * @return
     */
    private UserInfoModel do2UserInfo(MoocUserT moocUserT){
        UserInfoModel userInfoModel = new UserInfoModel();

        userInfoModel.setUuid(moocUserT.getUuid());
        userInfoModel.setHeadAddress(moocUserT.getHeadUrl());
        userInfoModel.setPhone(moocUserT.getUserPhone());
        userInfoModel.setUpdateTime(moocUserT.getUpdateTime().getTime());
        userInfoModel.setEmail(moocUserT.getEmail());
        userInfoModel.setUsername(moocUserT.getUserName());
        userInfoModel.setNickname(moocUserT.getNickName());
        userInfoModel.setLifeState(moocUserT.getLifeState()+"");
        userInfoModel.setBirthday(moocUserT.getBirthday());
        userInfoModel.setAddress(moocUserT.getAddress());
        userInfoModel.setSex(moocUserT.getUserSex());
        userInfoModel.setBeiginTime(moocUserT.getBeginTime().getTime());
        userInfoModel.setBiography(moocUserT.getBiography());

        return userInfoModel;

    }

    /**
     * 用户模型数据 转为 持久层类型
     * @param userInfoModel
     * @return
     */
    private MoocUserT UserInfo2do(UserInfoModel userInfoModel){
        MoocUserT moocUserT = new MoocUserT();

        moocUserT.setUuid(userInfoModel.getUuid());
        moocUserT.setHeadUrl(userInfoModel.getHeadAddress());
        moocUserT.setUserPhone(userInfoModel.getPhone());
        moocUserT.setUpdateTime(time2Date(System.currentTimeMillis()));
        moocUserT.setEmail(userInfoModel.getEmail());
        moocUserT.setUserName(userInfoModel.getUsername());
        moocUserT.setNickName(userInfoModel.getNickname());
        moocUserT.setLifeState(Integer.parseInt(userInfoModel.getLifeState()));
        moocUserT.setBirthday(userInfoModel.getBirthday());
        moocUserT.setAddress(userInfoModel.getAddress());
        moocUserT.setUserSex(userInfoModel.getSex());
        moocUserT.setBeginTime(time2Date(userInfoModel.getBeiginTime()));
        moocUserT.setBiography(userInfoModel.getBiography());

        return moocUserT;

    }


}
