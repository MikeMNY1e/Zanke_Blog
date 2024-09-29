package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.ArticleMapper;
import com.zanke.mapper.CategoryMapper;
import com.zanke.pojo.dto.CategoryListDto;
import com.zanke.pojo.entity.Category;
import com.zanke.pojo.vo.category.CategoryVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.CategoryService;
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
 * @description 分类服务实现类
 */
@Slf4j
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleServiceImpl articleService;

    @Resource
    private RedisTemplate<String, Category> redisTemplate;
    @Resource
    private RedissonClient redisson;


    /**
     * 获取有文章的分类列表
     * @return
     */
    @Override
    public ResponseResult<List<CategoryVo>> getCategoryList() {

        Set<Category> categorySet;

        // 查缓存
        try {
            categorySet = redisTemplate.opsForZSet().range(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey(), 0, -1);
            if (categorySet == null) {
                categorySet = Collections.emptySet();
            }
        } catch (Exception e) {
            throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
        }
        if (categorySet.isEmpty()) {
            // 加分布式锁
            RLock rLock = redisson.getLock(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey() + "Lock");
            try {
                rLock.lock();

                //再次查询缓存
                try {
                    categorySet = redisTemplate.opsForZSet().range(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey(), 0, -1);
                    if (categorySet == null) {
                        categorySet = Collections.emptySet();
                    }
                } catch (Exception e) {
                    throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                }
                if (categorySet.isEmpty()) {
                    // 查数据库
                    try {
                        // 查询所有已发布文章的分类
                        List<Long> publishedArticleCategoryList = articleMapper.selectPublishedArticleCategoryId();
                        List<Category> categoryList = categoryMapper.selectCategoryList(publishedArticleCategoryList, false);
                        categorySet = new LinkedHashSet<>(categoryList);
                    } catch (Exception e) {
                        log.error("所有有文章的分类查询数据库异常", e);
                    }

                    // 写入缓存
                    try {
                        Map<Category, Double> map =
                                categorySet.stream().collect(Collectors.toMap(category -> category, category -> (double) category.getId()));

                        Set<ZSetOperations.TypedTuple<Category>> collect = RedisUtil.getSetForZSetBatchAdd(map);

                        if (!collect.isEmpty()) {
                            redisTemplate.opsForZSet().add(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey(), collect);
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

        List<CategoryVo> categoryVoList = BeanCopyUtil.copyBeanCollectionToList(categorySet, CategoryVo.class);

        return ResponseResult.ok(categoryVoList);
    }


    /**
     * 获取有启用的分类列表
     * @return
     */
    @Override
    public ResponseResult<List<CategoryVo>> getEnableCategoryList() {

        List<CategoryVo> categoryVoList = categoryMapper.selectEnableCategoryList();

        return ResponseResult.ok(categoryVoList);
    }


    /**
     * 获取所有分类(导出分类excel用到)
     * @return
     */
    @Override
    public List<CategoryVo> getAllCategoryList() {
        return BeanCopyUtil.copyBeanCollectionToList(categoryMapper.selectCategoryList(null, true), CategoryVo.class);
    }


    /**
     * 获取分类列表(后台)
     * @param categoryListDto
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<CategoryVo>>> getCategoryListForAdmin(int pageNum, int pageSize, CategoryListDto categoryListDto) {

        if (categoryListDto.getName() != null && categoryListDto.getName().isEmpty()) {
            categoryListDto.setName(null);
        }
        if (categoryListDto.getStatus() != null && categoryListDto.getStatus().isEmpty()) {
            categoryListDto.setStatus(null);
        }

        List<CategoryVo> categoryVoList;
        try {
            PageHelper.startPage(pageNum, pageSize);
            categoryVoList = categoryMapper.selectAllCategoryListByCategoryAndStatus(categoryListDto);
        } finally {
            PageHelper.clearPage();
        }

        PageInfo<CategoryVo> categoryVoPageInfo = new PageInfo<>(categoryVoList);

        return ResponseResult.ok(new PageVo<>(categoryVoPageInfo.getTotal(), categoryVoPageInfo.getList()));
    }


    /**
     * 添加分类
     * @param category
     * @return
     */
    @Override
    public ResponseResult<Void> addCategory(Category category) {

        if (!StringUtils.hasText(category.getName())) {
            throw new SystemException(ResultCodeEnum.CATEGORY_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(category.getDescription())) {
            throw new SystemException(ResultCodeEnum.CATEGORY_DESCRIPTION_EMPTY_ERROR);
        }


        category.setCreateBy(AuthenticationUtil.getUserFromContextHolder().getId());
        category.setCreateTime(new Date());

        // 数据库添加分类
        categoryMapper.insert(category);

        return ResponseResult.ok(null);
    }


    /**
     * 获取分类详情
     * @param id
     * @return
     */
    @Override
    public ResponseResult<CategoryVo> getCategoryDetailById(Long id) {

        Category category = categoryMapper.selectCategoryByIdForAdmin(id);
        CategoryVo categoryVo = BeanCopyUtil.copyBean(category, CategoryVo.class);
        return ResponseResult.ok(categoryVo);
    }


    /**
     * 文章更新时，刷新文章详情页面不准更新浏览量，等重新创建文章zset再更新浏览量，防止zset中只有当前页面文章，其它文章丢失
     */
    private final String ARTICLE_ZSET_DELETE_LOCK = "article:zset:delete:lock";
    /**
     * 修改分类
     * @param category
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> updateCategory(Category category) {

        if (!StringUtils.hasText(category.getName())) {
            throw new SystemException(ResultCodeEnum.CATEGORY_NAME_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(category.getDescription())) {
            throw new SystemException(ResultCodeEnum.CATEGORY_DESCRIPTION_EMPTY_ERROR);
        }

        // 加分类锁，防止修改分类状态时，判断该分类下没文章后，有文章加入该分类
        RLock rLock = redisson.getLock(category.getId() + "Lock");
        try {
            rLock.lock();

            // 如果是禁用分类，分类下有文章则不可禁用
            List<Long> publishedArticleCategoryIdList = articleMapper.selectPublishedArticleCategoryId();
            if ("1".equals(category.getStatus()) && publishedArticleCategoryIdList.contains(category.getId())) {
                throw new SystemException(ResultCodeEnum.CATEGORY_HAS_ARTICLE_ERROR);
            }

            category.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            category.setUpdateTime(new Date());

            // 根据分类id获取旧分类名
            String oldCategoryName = categoryMapper.selectCategoryNameById(category.getId());

            // 数据库修改分类
            categoryMapper.updateCategory(category);

            // 如果改了分类名
            if (!oldCategoryName.equals(category.getName())) {
                RLock rLock1 = redisson.getLock(ARTICLE_ZSET_DELETE_LOCK);
                try {
                    rLock1.lock();
                    // 删除文章缓存zset，因为要更新文章的分类名字
                    redisTemplate.delete(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey());
                    // 创建新的缓存文章zet
                    articleService.getPublishedArticleListFromCacheByCategoryId();
                } finally {
                    if (rLock1.isHeldByCurrentThread()) {
                        rLock1.unlock();
                    }
                }
            }

            // 删除缓存分类集合
            redisTemplate.delete(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey());

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 删除分类
     * @param id
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> deleteCategory(Long id) {

        // 加分类锁，防止删除分类，判断该分类下没文章后，有文章加入该分类
        RLock rLock = redisson.getLock(id + "Lock");
        try {
            rLock.lock();

            // 分类有文章不可删除
            List<Long> publishedArticleCategoryIdList = articleMapper.selectPublishedArticleCategoryId();
            if (publishedArticleCategoryIdList.contains(id)) {
                throw new SystemException(ResultCodeEnum.CATEGORY_HAS_ARTICLE_ERROR);
            }

            // 数据库删除分类
            categoryMapper.deleteCategoryById(id);
            // 删除缓存分类集合
            redisTemplate.delete(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey());

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }
}