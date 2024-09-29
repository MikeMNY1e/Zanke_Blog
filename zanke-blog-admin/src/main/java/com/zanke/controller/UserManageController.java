package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.user.UserListDto;
import com.zanke.pojo.dto.user.UserStatusChangeDto;
import com.zanke.pojo.vo.user.AddUpdateUserVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.user.UserUpdateVo;
import com.zanke.pojo.vo.user.UserVo;
import com.zanke.service.UserManageService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/system/user")
public class UserManageController {

    @Resource
    private UserManageService userManageService;


    @GetMapping("/list")
    @UrlLogBusiness("获取用户列表")
    public ResponseResult<PageVo<List<UserVo>>> getAdminUserList(int pageNum, int pageSize, UserListDto userListDto) {
        return userManageService.getAdminUserList(pageNum, pageSize, userListDto);
    }


    @PostMapping
    @UrlLogBusiness("新增用户")
    public ResponseResult<Void> addUser(@RequestBody AddUpdateUserVo addUserVo) {
        return userManageService.addUser(addUserVo);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除用户")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        return userManageService.deleteUser(id);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取用户详情")
    public ResponseResult<UserUpdateVo> getUserDetailById(@PathVariable Long id) {
        return userManageService.getUserDetailById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改用户信息")
    public ResponseResult<Void> updateUser(@RequestBody AddUpdateUserVo addUserVo) {
        return userManageService.updateUser(addUserVo);
    }


    @PutMapping("/changeStatus")
    @UrlLogBusiness("修改角色状态")
    public ResponseResult<Void> changeStatus(@RequestBody UserStatusChangeDto userStatusChangeDto) {
        return userManageService.changeStatus(userStatusChangeDto);
    }
}
