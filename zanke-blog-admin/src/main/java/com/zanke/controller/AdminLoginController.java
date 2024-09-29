package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.AdminUserInfoVo;
import com.zanke.pojo.vo.user.LoginUserInfoVo;
import com.zanke.pojo.vo.menu.RoutersVo;
import com.zanke.service.LoginService;
import com.zanke.service.MenuService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;



/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
public class AdminLoginController {

    @Resource
    private LoginService loginService;
    @Resource
    private MenuService menuService;


    @PostMapping("/user/login")
    @UrlLogBusiness("管理员登录")
    public ResponseResult<LoginUserInfoVo> login(@RequestBody User user) {
        return loginService.login(user, true);
    }


    @GetMapping("/getInfo")
    @UrlLogBusiness("获取管理员信息")
    public ResponseResult<AdminUserInfoVo> getInfo() {
        return menuService.getInfoByUserId();
    }

    @GetMapping("/getRouters")
    @UrlLogBusiness("获取管理员路由菜单")
    public ResponseResult<RoutersVo> getRouters() {
        return menuService.selectRouterMenuTreeByUserId();
    }


    @PostMapping("/user/logout")
    @UrlLogBusiness("管理员退出")
    public ResponseResult<Void> logout() {
        return ResponseResult.ok(null);
    }
}
