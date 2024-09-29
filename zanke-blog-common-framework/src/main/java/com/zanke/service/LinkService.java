package com.zanke.service;

import com.zanke.pojo.dto.link.LinkListDto;
import com.zanke.pojo.dto.link.LinkStatusChangeDto;
import com.zanke.pojo.entity.Link;
import com.zanke.pojo.vo.LinkVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface LinkService {

    /**
     * 获取所有友链
     * @return
     */
    ResponseResult<List<LinkVo>> getAllLink();


    /**
     * 获取所有友链(后台)
     * @return
     */
    ResponseResult<PageVo<List<LinkVo>>> getAllLinkForAdmin(int pageNum, int pageSize, LinkListDto linkListDto);


    /**
     * 添加友链
     * @param link
     * @return
     */
    ResponseResult<Void> addLink(Link link);


    /**
     * 获取友链信息
     * @param id
     * @return
     */
    ResponseResult<LinkVo> getLinkInfoById(Long id);


    /**
     * 修改友链
     * @param link
     * @return
     */
    ResponseResult<Void> updateLink(Link link);


    /**
     *  修改友链状态
     * @param linkStatusChangeDto
     * @return
     */
    ResponseResult<Void> changeStatus(LinkStatusChangeDto linkStatusChangeDto);


    /**
     * 删除友链
     * @param id
     * @return
     */
    ResponseResult<Void> deleteLink(Long id);
}
