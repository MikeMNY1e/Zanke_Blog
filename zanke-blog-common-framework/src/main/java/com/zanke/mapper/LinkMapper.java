package com.zanke.mapper;

import com.zanke.pojo.dto.link.LinkListDto;
import com.zanke.pojo.entity.Link;
import com.zanke.pojo.vo.LinkVo;

import java.util.List;


/**
 * @author Zanke
 * @version 1.0.0
 * @description 友链Mapper
 */
public interface LinkMapper {

    /**
     * 查询所有友链
     * @return
     */
    List<Link> selectAllLink();


    /**
     * 根据友链名称和状态查询友链
     * @param linkListDto
     * @return
     */
    List<LinkVo> selectAllLinkByLinkNameAndStatus(LinkListDto linkListDto);


    /**
     * 新增友链
     * @param link
     * @return
     */
    int insert(Link link);


    /**
     * 根据id查询友链
     * @param id
     * @return
     */
    LinkVo selectCategoryById(Long id);


    /**
     * 修改友链
     * @param link
     * @return
     */
    int updateLink(Link link);


    /**
     * 修改友链状态
     * @param link
     * @return
     */
    int updateStatus(Link link);


    /**
     * 删除友链
     * @param id
     * @return
     */
    int deleteLink(Long id);
}
