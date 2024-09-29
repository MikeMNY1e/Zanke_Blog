package com.zanke.mapper;

import com.zanke.pojo.entity.User;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    User selectByUsername(String username);


    /**
     * 更新用户信息
     * @return
     */
    int update(User user);


    /**
     * 新增用户
     * @param user
     * @return
     */
    int insert(User user);


    /**
     * 查询所有用户名
     * @return
     */
    List<String> selectAllUsername();


    /**
     * 查询所有昵称
     * @return
     */
    List<String> selectAllNickName();


    /**
     * 根据id查询昵称
     * @param toCommentUserId
     * @return
     */
    String selectNickNameById(Long toCommentUserId);
}
