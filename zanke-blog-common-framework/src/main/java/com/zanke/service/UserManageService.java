package com.zanke.service;

import com.zanke.pojo.dto.user.UserListDto;
import com.zanke.pojo.dto.user.UserStatusChangeDto;
import com.zanke.pojo.vo.user.AddUpdateUserVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.user.UserUpdateVo;
import com.zanke.pojo.vo.user.UserVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UserManageService {

    /**
     * 获取用户列表(后台)
     * @return
     */
    ResponseResult<PageVo<List<UserVo>>> getAdminUserList(int pageNum, int pageSize, UserListDto userListDto);


    /**
     *  新增用户
     * @param addUserVo
     * @return
     */
    ResponseResult<Void> addUser(AddUpdateUserVo addUserVo);


    /**
     *  删除用户
     * @param id
     * @return
     */
    ResponseResult<Void> deleteUser(Long id);


    /**
     *  获取用户详情
     * @param id
     * @return
     */
    ResponseResult<UserUpdateVo> getUserDetailById(Long id);


    /**
     *  修改用户信息
     * @param addUserVo
     * @return
     */
    ResponseResult<Void> updateUser(AddUpdateUserVo addUserVo);


    /**
     *  修改用户状态
     * @param userStatusChangeDto
     * @return
     */
    ResponseResult<Void> changeStatus(UserStatusChangeDto userStatusChangeDto);
}
