package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.ArticleTagMapper;
import com.zanke.mapper.TagMapper;
import com.zanke.pojo.dto.TagListDto;
import com.zanke.pojo.entity.Tag;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.PageVo;
import com.zanke.pojo.vo.TagVo;
import com.zanke.service.TagService;
import com.zanke.util.AuthenticationUtil;
import com.zanke.util.BeanCopyUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Service("tagService")
public class TagServiceImpl implements TagService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;

    @Resource
    private RedissonClient redisson;


    /**
     * 分页查询标签列表
     *
     * @param pageNum
     * @param pageSize
     * @param tagListDto
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<TagVo>>> pageTagList(int pageNum, int pageSize, TagListDto tagListDto) {

        if (tagListDto.getName() != null && tagListDto.getName().isEmpty()) {
            tagListDto.setName(null);
        }
        if (tagListDto.getRemark() != null && tagListDto.getRemark().isEmpty()) {
            tagListDto.setRemark(null);
        }

        // 分页查询
        List<Tag> tagList;
        try {
            PageHelper.startPage(pageNum, pageSize);
            tagList = tagMapper.selectTagList(tagListDto);
        } finally {
            PageHelper.clearPage();
        }

        List<TagVo> tagVoList = BeanCopyUtil.copyBeanCollectionToList(tagList, TagVo.class);
        PageInfo<TagVo> tagPageInfo = new PageInfo<>(tagVoList);

        return ResponseResult.ok(new PageVo<>(tagPageInfo.getTotal(), tagVoList));
    }


    /**
     * 添加标签
     * @param tag
     * @return
     */
    @Override
    public ResponseResult<Void> addTag(Tag tag) {

        // 校验标签名称和标签描述
        if (!StringUtils.hasText(tag.getName())) {
            throw new SystemException(ResultCodeEnum.TAG_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(tag.getRemark())) {
            throw new SystemException(ResultCodeEnum.TAG_REMARK_EMPTY_ERROR);
        }

        // 封装标签
        Date date = new Date();
        tag.setCreateTime(date);
        User user = AuthenticationUtil.getUserFromContextHolder();
        tag.setCreateBy(user.getId());
        tag.setUpdateTime(date);
        tag.setUpdateBy(user.getId());

        int affectRows = tagMapper.insertInt(tag);
        if (affectRows > 0) {
            return ResponseResult.ok(null);
        } else {
            throw new SystemException(ResultCodeEnum.TAG_ADD_ERROR);
        }
    }


    /**
     * 根据标签id删除标签
     * @param id
     * @return
     */
    @Override
    public ResponseResult<Void> deleteTagById(Long id) {

        RLock rLock = redisson.getLock(id + "Lock");
        try {

            if (articleTagMapper.selectArticleIdByTagId(id) > 0) {
                throw new SystemException(ResultCodeEnum.TAG_HAS_ARTICLE_ERROR);
            }

            rLock.lock();
            Tag tag = new Tag();
            tag.setId(id);
            tag.setDelFlag(1);

            // 标签删除标记改为1
            int affectRows = tagMapper.updateTagById(tag);
            // 删除文章标签关系
            articleTagMapper.deleteByTagId(id);
            if (affectRows > 0) {
                return ResponseResult.ok(null);
            } else {
                throw new SystemException(ResultCodeEnum.TAG_DELETE_ERROR);
            }
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }


    /**
     * 根据标签id查询标签
     * @param id
     * @return
     */
    @Override
    public ResponseResult<TagVo> getTagInfoById(Long id) {

        Tag tag = tagMapper.selectTagById(id);
        return ResponseResult.ok(BeanCopyUtil.copyBean(tag, TagVo.class));
    }


    /**
     * 更新标签
     * @param tag
     * @return
     */
    @Override
    public ResponseResult<Void> updateTagById(Tag tag) {

        // 校验标签名称和标签描述
        if (!StringUtils.hasText(tag.getName())) {
            throw new SystemException(ResultCodeEnum.TAG_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(tag.getRemark())) {
            throw new SystemException(ResultCodeEnum.TAG_REMARK_EMPTY_ERROR);
        }

        tag.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        tag.setUpdateTime(new Date());
        int affectRows = tagMapper.updateTagById(tag);
        if (affectRows > 0) {
            return ResponseResult.ok(null);
        } else {
            throw new SystemException(ResultCodeEnum.TAG_UPDATE_ERROR);
        }
    }


    /**
     * 获取所有标签(写文章用到)
     * @return
     */
    @Override
    public ResponseResult<List<TagVo>> allTagList() {

        List<Tag> tagList = tagMapper.selectTagList(new TagListDto());
        return ResponseResult.ok(BeanCopyUtil.copyBeanCollectionToList(tagList, TagVo.class));
    }
}