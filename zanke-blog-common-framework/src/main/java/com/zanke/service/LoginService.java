package com.zanke.service;

import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.LoginUserInfoVo;
import com.zanke.util.ResponseResult;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface LoginService {

    /**
     * 用户登录
     * @param user
     * @return
     */
    ResponseResult<LoginUserInfoVo> login(User user, boolean isAdmin);
}
