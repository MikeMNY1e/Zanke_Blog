package com.zanke.mapper;

import com.zanke.pojo.dto.user.UserStatusChangeDto;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface UserRoleMapper {

    /**
     * 插入用户角色关联
     * @param id
     * @param roleIds
     * @return
     */
    int insertUserRole(Long id, List<Long> roleIds, String status);


    /**
     * 根据用户id删除用户角色关联
     * @param id
     * @return
     */
    int deleteByUserId(Long id);


    /**
     * 根据用户id查询角色id列表
     * @param id
     * @return
     */
    List<Long> selectRoleIdListByUserId(Long id);


    /**
     * 修改用户状态
     * @param userStatusChangeDto
     * @return
     */
    int updateStatus(UserStatusChangeDto userStatusChangeDto);


    /**
     * 根据角色id查询用户数量
     * @param roleId
     * @return
     */
    int selectUserCountByRoleId(Long roleId);
}
