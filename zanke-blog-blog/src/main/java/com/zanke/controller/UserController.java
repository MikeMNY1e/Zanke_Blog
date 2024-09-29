package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.UserInfoVo;
import com.zanke.service.UserService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/userInfo")
    @UrlLogBusiness("获取用户信息")
    public ResponseResult<UserInfoVo> getUserInfo(){
        return userService.getUserInfo();
    }


    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @PutMapping("/userInfo")
    @UrlLogBusiness("修改用户信息")
    public ResponseResult<Void> updateUserInfo(@RequestBody User user){
        return userService.updateUserInfo(user);
    }


    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/register")
    @UrlLogBusiness("用户注册")
    public ResponseResult<Void> register(@RequestBody User user){
        return userService.register(user);
    }
}
