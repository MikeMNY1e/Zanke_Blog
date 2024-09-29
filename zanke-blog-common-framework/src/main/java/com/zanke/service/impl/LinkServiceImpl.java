package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.LinkMapper;
import com.zanke.pojo.dto.link.LinkListDto;
import com.zanke.pojo.dto.link.LinkStatusChangeDto;
import com.zanke.pojo.entity.Link;
import com.zanke.pojo.vo.LinkVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.LinkService;
import com.zanke.util.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description 友链服务实现类
 */
@Slf4j
@Service("linkService")
public class LinkServiceImpl implements LinkService {

    @Resource
    private LinkMapper linkMapper;

    @Resource
    private RedisTemplate<String, Link> redisTemplate;
    @Resource
    private RedissonClient redisson;


    @Override
    public ResponseResult<List<LinkVo>> getAllLink() {

        Set<Link> linkSet;

        // 查缓存
        try {
            linkSet = redisTemplate.opsForZSet().range(RedisKeyEnum.LINK_SET_KEY.getKey(), 0, -1);
            if (linkSet == null) {
                linkSet = Collections.emptySet();
            }
        } catch (Exception e) {
            throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
        }
        if (linkSet.isEmpty()) {
            // 加分布式锁
            RLock rLock = redisson.getLock(RedisKeyEnum.LINK_SET_KEY.getKey() + "Lock");
            try {
                rLock.lock();

                //再次查询缓存
                try {
                    linkSet = redisTemplate.opsForZSet().range(RedisKeyEnum.LINK_SET_KEY.getKey(), 0, -1);
                    if (linkSet == null) {
                        linkSet = Collections.emptySet();
                    }
                } catch (Exception e) {
                    throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                }
                if (linkSet.isEmpty()) {
                    // 查数据库
                    try {
                        List<Link> linkList = linkMapper.selectAllLink();
                        linkSet = new LinkedHashSet<>(linkList);
                    } catch (Exception e) {
                        log.error("所有友链查询数据库异常", e);
                    }

                    // 写入缓存
                    try {
                        Map<Link, Double> map =
                                linkSet.stream().collect(Collectors.toMap(link -> link, link -> (double) link.getId()));

                        Set<ZSetOperations.TypedTuple<Link>> collect = RedisUtil.getSetForZSetBatchAdd(map);

                        if (!collect.isEmpty()) {
                            redisTemplate.opsForZSet().add(RedisKeyEnum.LINK_SET_KEY.getKey(), collect);
                        }

                    } catch (Exception e) {
                        log.error("所有友链写入缓存异常", e);
                    }
                }

            } finally {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            }
        }

        List<LinkVo> linkVoList = BeanCopyUtil.copyBeanCollectionToList(linkSet, LinkVo.class);

        return ResponseResult.ok(linkVoList);
    }


    /**
     * 获取所有友链
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<LinkVo>>> getAllLinkForAdmin(int pageNum, int pageSize, LinkListDto linkListDto) {

        if (linkListDto.getName() != null && linkListDto.getName().isEmpty()) {
            linkListDto.setName(null);
        }
        if (linkListDto.getStatus() != null && linkListDto.getStatus().isEmpty()) {
            linkListDto.setStatus(null);
        }

        List<LinkVo> linkList;
        try {
            PageHelper.startPage(pageNum, pageSize);
            linkList = linkMapper.selectAllLinkByLinkNameAndStatus(linkListDto);
        } finally {
            PageHelper.clearPage();
        }
        PageInfo<LinkVo> linkVoPageInfo = new PageInfo<>(linkList);

        return ResponseResult.ok(new PageVo<>(linkVoPageInfo.getTotal(), linkList));
    }


    /**
     *
     * @param link
     * @return
     */
    @Override
    @Transactional
    public ResponseResult<Void> addLink(Link link) {

        if (!StringUtils.hasText(link.getName())) {
            throw new SystemException(ResultCodeEnum.LINK_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getDescription())) {
            throw new SystemException(ResultCodeEnum.DESCRIPTION_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getLogo())) {
            throw new SystemException(ResultCodeEnum.LOGO_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getAddress())) {
            throw new SystemException(ResultCodeEnum.ADDRESS_EMPTY_ERROR);
        }


        link.setCreateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        link.setCreateTime(new Date());
        // 数据库添加友链
        linkMapper.insert(link);
        // 缓存删除友链集合
        redisTemplate.delete(RedisKeyEnum.LINK_SET_KEY.getKey());

        return ResponseResult.ok(null);
    }


    /**
     * 获取友链信息
     * @param id
     * @return
     */
    @Override
    public ResponseResult<LinkVo> getLinkInfoById(Long id) {

        LinkVo linkVo = linkMapper.selectCategoryById(id);
        return ResponseResult.ok(linkVo);
    }


    /**
     * 修改友链
     * @param link
     * @return
     */
    @Override
    @Transactional
    public ResponseResult<Void> updateLink(Link link) {

        if (!StringUtils.hasText(link.getName())) {
            throw new SystemException(ResultCodeEnum.LINK_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getDescription())) {
            throw new SystemException(ResultCodeEnum.DESCRIPTION_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getLogo())) {
            throw new SystemException(ResultCodeEnum.LOGO_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(link.getAddress())) {
            throw new SystemException(ResultCodeEnum.ADDRESS_EMPTY_ERROR);
        }

        link.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        link.setUpdateTime(new Date());
        // 数据库修改友链
        linkMapper.updateLink(link);
        // 缓存删除友链集合
        redisTemplate.delete(RedisKeyEnum.LINK_SET_KEY.getKey());

        return ResponseResult.ok(null);
    }


    /**
     *  修改友链状态
     * @param linkStatusChangeDto
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> changeStatus(LinkStatusChangeDto linkStatusChangeDto) {

        Link link = new Link();
        link.setId(linkStatusChangeDto.getId());
        link.setStatus(linkStatusChangeDto.getStatus());
        link.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        link.setUpdateTime(new Date());
        // 数据库修改友链
        linkMapper.updateStatus(link);
        // 缓存删除友链集合
        redisTemplate.delete(RedisKeyEnum.LINK_SET_KEY.getKey());

        return ResponseResult.ok(null);
    }


    /**
     * 删除友链
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ResponseResult<Void> deleteLink(Long id) {

        // 数据库逻辑删除友链
        linkMapper.deleteLink(id);
        // 缓存删除友链集合
        redisTemplate.delete(RedisKeyEnum.LINK_SET_KEY.getKey());

        return ResponseResult.ok(null);
    }
}
