package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.LoginUserInfoVo;
import com.zanke.service.LoginService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
public class BlogLoginController {

    @Resource
    private LoginService blogUserService;


    /**
     * 用户登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    @UrlLogBusiness("用户登录")
    public ResponseResult<LoginUserInfoVo> login(@RequestBody User user) {
        return blogUserService.login(user, false);
    }


    /**
     * 用户登出
     * @return
     */
    @PostMapping("/logout")
    @UrlLogBusiness("用户登出")
    public ResponseResult<Void> logout() {
        // 不用实现，前端直接删除token即可
        return ResponseResult.ok(null);
    }
}
