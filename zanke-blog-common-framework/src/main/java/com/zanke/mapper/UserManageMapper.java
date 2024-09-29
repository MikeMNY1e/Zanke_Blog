package com.zanke.mapper;

import com.zanke.pojo.dto.user.UserListDto;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.user.UserVo;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UserManageMapper {

    /**
     * 根据用户名和手机号和状态查询用户
     * @param userListDto
     * @return
     */
    List<UserVo> selectUserByUserNameAndPhonenumAndStatus(UserListDto userListDto);


    /**
     * 新增用户
     * @param user
     */
    int insert(User user);


    /**
     * 删除用户
     * @param id
     * @return
     */
    int deleteUser(Long id);


    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User selectUserById(Long id);


    /**
     * 修改用户
     * @param user
     * @return
     */
    int updateUser(User user);


    /**
     * 修改用户状态
     * @param user
     */
    int updateStatus(User user);


    /**
     * 根据id查询用户名
     * @param id
     * @return
     */
    String selectUsernameById(Long id);
}
