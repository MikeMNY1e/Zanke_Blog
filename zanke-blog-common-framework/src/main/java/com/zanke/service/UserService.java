package com.zanke.service;

import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.UserInfoVo;
import com.zanke.util.ResponseResult;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UserService {

    /**
     * 获取用户信息
     * @return
     */
    ResponseResult<UserInfoVo> getUserInfo();


    /**
     * 修改用户信息
     * @param user
     * @return
     */
    ResponseResult<Void> updateUserInfo(User user);


    /**
     * 添加用户
     * @param user
     * @return
     */
    ResponseResult<Void> register(User user);
}
