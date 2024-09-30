package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.*;
import com.zanke.pojo.dto.article.AddArticleDto;
import com.zanke.pojo.dto.article.ArticleListDto;
import com.zanke.pojo.entity.Article;
import com.zanke.pojo.entity.Tag;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.*;
import com.zanke.pojo.vo.article.ArticleDetailVo;
import com.zanke.pojo.vo.article.ArticleVo;
import com.zanke.pojo.vo.article.HotArticleVo;
import com.zanke.pojo.vo.category.CategoryCheckVo;
import com.zanke.schedule.UpdateViewCountSchedule;
import com.zanke.service.ArticleService;
import com.zanke.util.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description Article相关业务接口实现类
 */
@Slf4j
@Service("articleService")
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redisson;

    @Resource
    private UpdateViewCountSchedule updateViewCountSchedule;

    /**
     * 热门文章展示个数
     */
    private final int HOT_ARTICLE_NUM = 10;


    /**
     * 获取已发布热门文章
     *
     * @return
     */
    @Override
    public ResponseResult<List<HotArticleVo>> getHotArticleList() {

        // 从缓存中获取已发布文章列表
        List<Article> publishedArticleList =
                getPublishedArticleListFromCacheByCategoryId();

        // 分页
        List<Article> hotArticleList = publishedArticleList.stream().limit(HOT_ARTICLE_NUM).toList();

        //转化为 HotArticleVo
        List<HotArticleVo> hotArticleVoList =
                BeanCopyUtil.copyBeanCollectionToList(hotArticleList, HotArticleVo.class);

        return ResponseResult.ok(hotArticleVoList);
    }


    /**
     * 根据分类id(0表示所有文章)，分页获取已发布文章列表
     * @param pageNum
     * @param pageSize
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<ArticleVo>>> getArticleListByCategoryId(int pageNum, int pageSize, Long categoryId) {

        // 从缓存中分页获取已发布文章列表
        List<Article> publishedArticleList =
                getPublishedArticleListFromCacheByCategoryId();

        // 获取对应分类的文章
        if (categoryId != 0) {
            publishedArticleList = publishedArticleList.stream().filter(article -> article.getCategoryId().equals(categoryId)).toList();
        }
        Long total = (long) publishedArticleList.size();

        //分页
        List<Article> list = publishedArticleList.stream().skip((long) (pageNum - 1) * pageSize).limit(pageSize).toList();

        // 转化为 ArticleVo
        List<ArticleVo> articleVoList = BeanCopyUtil.copyBeanCollectionToList(list, ArticleVo.class);

        // 封装分页信息
        PageVo<List<ArticleVo>> pageVo = new PageVo<>();
        pageVo.setRows(articleVoList);
        pageVo.setTotal(total);

        return ResponseResult.ok(pageVo);
    }

    /**
     * 根据分类id(0表示所有文章)，分页获取已发布文章列表
     * @return
     */
    public List<Article> getPublishedArticleListFromCacheByCategoryId() {

        // 加文章zset删除锁，因为必须保证删除文章zset的操作后是在该操作内回复的文章zset
        RLock rLock1 = redisson.getLock(ARTICLE_ZSET_DELETE_LOCK);

        List<Article> articleList = Collections.emptyList();
        Set<Object> articleIdSet = Collections.emptySet();

        try {
            rLock1.lock();

            // 查询缓存
            boolean isArticleDetailHashExisits = true;

            try {
                articleIdSet = redisTemplate.opsForZSet().range(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), 0, -1);
                if (articleIdSet == null) {
                    articleIdSet = Collections.emptySet();
                }

                // 缓存有文章zset和文章详情hash才查缓存
                isArticleDetailHashExisits =
                        Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey()));

                if (!articleIdSet.isEmpty() && isArticleDetailHashExisits) {
                    List<String> fieldList = articleIdSet.stream().map(id -> RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id).toList();
                    articleList = redisTemplate.<String, Article>opsForHash().multiGet(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), fieldList);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (articleIdSet.isEmpty() || !isArticleDetailHashExisits) {

                // 加分布式锁
                RLock rLock = redisson.getLock(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey() + "Lock");

                try {
                    rLock.lock();

                    // 再次查询缓存防止缓存穿透
                    try {
                        articleIdSet = redisTemplate.opsForZSet().range(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), 0, -1);
                        if (articleIdSet == null) {
                            articleIdSet = Collections.emptySet();
                        }

                        // 缓存有文章zset和文章详情hash才查缓存
                        isArticleDetailHashExisits =
                                Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey()));

                        if (!articleIdSet.isEmpty() && isArticleDetailHashExisits) {
                            List<String> fieldList = articleIdSet.stream().map(id -> RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id).toList();
                            articleList = redisTemplate.<String, Article>opsForHash().multiGet(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), fieldList);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (articleIdSet.isEmpty() || !isArticleDetailHashExisits) {

                        // 查数据库
                        try {
                            articleList = articleMapper.selectPublishedArticleListDESCByIsTopAndViewCount();

                        } catch (Exception e) {
                            log.error("分类发布文章列表数据库查询出现异常", e);
                        }

                        try {
                            // 写入缓存

                            // 写文章id的zset，根据浏览量降序
                            Map<Object, Double> map = articleList.stream()
                                    .collect(Collectors.toMap(Article::getId, article -> -1d * article.getViewCount()));

                            Set<ZSetOperations.TypedTuple<Object>> collect = RedisUtil.getSetForZSetBatchAdd(map);

                            if (!collect.isEmpty()) {
                                // 缓存写文章id的zset，根据浏览量降序
                                redisTemplate.opsForZSet().add(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), collect);

                                // 写文章详情hash
                                Map<String, Article> fieldValueMap =
                                        articleList.stream().collect(Collectors.toMap(article -> RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + article.getId(), article -> article));

                                redisTemplate.opsForHash().putAll(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), fieldValueMap);
                            }

                        } catch (Exception e) {
                            log.error("将文章列表写入缓存异常", e);
                        }
                    }

                } finally {
                    // 确保当前线程持有自己的锁
                    if (rLock.isHeldByCurrentThread()) {
                        // 释放锁
                        rLock.unlock();
                    }
                }
            }
            
        } finally {

            if (rLock1.isHeldByCurrentThread()) {
                rLock1.unlock();
            }
        }

        return articleList;
    }


    /**
     * 根据文章id获取已发布文章详情接口
     * @param id 文章id
     * @return 文章详情vo
     */
    @Override
    public ResponseResult<ArticleDetailVo> getArticleDetailById(Long id) {

        // 获取文章详情
        Article article = getArticleDetailByIdFromCache(id);
        // 转化为vo
        ArticleDetailVo articleDetail = BeanCopyUtil.copyBean(article, ArticleDetailVo.class);

        return ResponseResult.ok(articleDetail);
    }
    /**
     * 根据文章id获取文章详情
     * @param id
     * @return
     */
    private Article getArticleDetailByIdFromCache(Long id) {

        String field = RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id;
        Article article;

        // 查询缓存
        article = redisTemplate.<String, Article>opsForHash().get(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), field);

        if (article == null) {

            RLock rLock = redisson.getLock(RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id + "Lock");
            try {
                // 加分布式锁
                rLock.lock();

                try {
                    // 再次查询缓存
                    article = redisTemplate.<String, Article>opsForHash().get(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), field);
                } catch (Exception e) {
                    log.error("缓存查询文章详情异常", e);
                }

                if (article == null) {
                    try {
                        // 查数据库
                        article = articleMapper.selectPublishedArticleByIdDESCByViewCount(id);
                    } catch (Exception e) {
                        log.error("数据库查询文章详情异常", e);
                    }

                    try {
                        // 写入缓存
                        if (article != null) {
                            redisTemplate.opsForHash().put(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), field, article);
                        }
                    } catch (Exception e) {
                        log.error("将文章详情写入缓存Redis出现异常", e);
                    }
                }

            } finally {
                if (rLock.isHeldByCurrentThread()) {
                    // 释放锁
                    rLock.unlock();
                }
            }
        }

        return article;
    }


    /**
     * 更新文章浏览量
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult<Void> updateViewCount(Long id, HttpServletRequest request) {

        // 获取当前登录用户
        User user =  AuthenticationUtil.getUserFromContextHolder();

        // 使用redis的hyperloglog实现浏览量UV
        if (user != null) {
            // 浏览量hll写入用户名
            redisTemplate.opsForHyperLogLog()
                    .add(RedisKeyEnum.ARTICLE_VIEWCOUNT_HLL_PREFIX.getKey() + id, user.getUsername());
        } else {
            // 没有登录的话浏览量hll写入客户端ip
            // 获取请求的ip
            redisTemplate.opsForHyperLogLog()
                    .add(RedisKeyEnum.ARTICLE_VIEWCOUNT_HLL_PREFIX.getKey() + id, request.getRemoteHost());
        }

        // 获取浏览量
        Long nowViewCount = redisTemplate.opsForHyperLogLog()
                .size(RedisKeyEnum.ARTICLE_VIEWCOUNT_HLL_PREFIX.getKey() + id);

        Article article = getArticleDetailByIdFromCache(id);
        article.setViewCount(nowViewCount);

        RLock rLock = redisson.getLock(ARTICLE_ZSET_DELETE_LOCK);
        try {
            rLock.lock();
            // 修改缓存中的 文章详情hash、所有文章id的zset
            String luaScript = """
                    local function updateViewCount()
                        redis.call('HSET', KEYS[1], KEYS[2], ARGV[1])
                        redis.call('ZADD', KEYS[3], ARGV[2], ARGV[3])
                    end
                    return updateViewCount()""";

            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Void.class),
                    List.of(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(),
                            RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id,
                            RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey()),
                    article,
                    -1d * article.getViewCount(),
                    article.getId()
            );
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 添加文章
     *
     * @param addArticleDto
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> addArticle(AddArticleDto addArticleDto) {

        // 参数校验
        if (!StringUtils.hasText(addArticleDto.getTitle())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_TITLE_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(addArticleDto.getSummary())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_SUMMARY_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(addArticleDto.getContent())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_CONTENT_EMPTY_ERROR);
        }
        if (addArticleDto.getTags().isEmpty()) {
            throw new SystemException(ResultCodeEnum.ARTICLE_TAG_EMPTY_ERROR);
        }
        if (addArticleDto.getCategoryId() == null) {
            throw new SystemException(ResultCodeEnum.ARTICLE_CATEGORY_EMPTY_ERROR);
        }

        // 加分类锁，防止修改分类状态或删除分类时，判断分类下没文章后，有文章加入分类
        RLock rLock = redisson.getLock(addArticleDto.getCategoryId() + "Lock");
        List<RLock> rLockList = new ArrayList<>();
        for (Long tagId : addArticleDto.getTags()) {
            RLock rLock1 = redisson.getLock(tagId + "Lock");
            rLockList.add(rLock1);
        }
        try {
            rLock.lock();
            for (RLock lock : rLockList) {
                lock.lock();
            }

            // 添加前再次检查分类是否被删除
            CategoryCheckVo categoryCheckVo = categoryMapper.selectStatusAndDeleteFlagById(addArticleDto.getCategoryId());
            if (!"0".equals(categoryCheckVo.getStatus()) || !"0".equals(categoryCheckVo.getDelFlag())) {
                throw new SystemException(ResultCodeEnum.CATEGORY_ILLEGAL_ERROR);
            }
            // 检查标签是否被删除
            if (tagMapper.selectDisableTagCountByIds(addArticleDto.getTags()) > 0) {
                throw new SystemException(ResultCodeEnum.TAG_ILLEGAL);
            }

            // 封装article
            Article article = BeanCopyUtil.copyBean(addArticleDto, Article.class);
            Date date = new Date();
            article.setCreateTime(date);
            article.setUpdateTime(date);
            User user = AuthenticationUtil.getUserFromContextHolder();
            article.setCreateBy(user.getId());
            article.setUpdateBy(user.getId());
            article.setViewCount(0L);
            article.setCategoryName(categoryMapper.selectCategoryNameById(article.getCategoryId()));

            // 数据库添加文章
            articleMapper.insert(article);

            // 添加文章标签关系
            articleTagMapper.insertIntoArticleTag(article.getId(), addArticleDto.getTags());

            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                    // 删除文章缓存zset前，更新数据库浏览量
                    updateViewCountSchedule.updateViewCount();

                    RLock rLock1 = redisson.getLock(ARTICLE_ZSET_DELETE_LOCK);
                    try {
                        rLock1.lock();

                        // 删除缓存文章zet and 删除分类缓存
                        redisTemplate.delete(List.of(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), RedisKeyEnum.CATEGORY_ZSET_KEY.getKey()));

                        // 创建新的缓存文章zet
                        getPublishedArticleListFromCacheByCategoryId();

                    } finally {
                        if (rLock1.isHeldByCurrentThread()) {
                            rLock1.unlock();
                        }
                    }

                    // 删除分类缓存zset
                    redisTemplate.delete(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey());

                    return null;
                }
            });

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
            for (RLock lock : rLockList) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 获取全部文章列表(包括草稿)
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<ArticleVo>>> getArticleListForAdmin(int pageNum, int pageSize, ArticleListDto articleListDto) {

        if (articleListDto.getTitle() != null && articleListDto.getTitle().isEmpty()) {
            articleListDto.setTitle(null);
        }
        if (articleListDto.getSummary() != null && articleListDto.getSummary().isEmpty()) {
            articleListDto.setSummary(null);
        }

        List<Article> articleList;
        try {
            PageHelper.startPage(pageNum, pageSize);
            articleList = articleMapper.selectArticleListByTitleAndSummary(articleListDto);
        } finally {
            PageHelper.clearPage();
        }
        PageInfo<Article> pageInfo = new PageInfo<>(articleList);
        List<ArticleVo> articleVoList = BeanCopyUtil.copyBeanCollectionToList(articleList, ArticleVo.class);
        return ResponseResult.ok(new PageVo<>(pageInfo.getTotal(), articleVoList));
    }


    /**
     * 获取文章详情(后台)
     * @param id
     * @return
     */
    @Override
    public ResponseResult<Article> getArticleDetailForAdmin(Long id) {

        // 获取文章详情
        Article article = articleMapper.selectArticleForAdmin(id);
        // 获取文章标签
        List<Tag> tagList = tagMapper.selectTagListByArticleId(id);
        article.setTags(tagList.stream().map(Tag::getId).toList());

        return ResponseResult.ok(article);
    }


    /**
     * 文章更新时，刷新文章详情页面不准更新浏览量，等重新创建文章zset再更新浏览量，防止zset中只有当前页面文章，其它文章丢失
     */
    private final String ARTICLE_ZSET_DELETE_LOCK = "article:zset:delete:lock";
    /**
     * 更新文章
     * @param article
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> updateArticle(Article article) {

        // 参数校验
        if (!StringUtils.hasText(article.getTitle())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_TITLE_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(article.getSummary())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_SUMMARY_EMPTY_ERROR);
        }
        if (!StringUtils.hasText(article.getContent())) {
            throw new SystemException(ResultCodeEnum.ARTICLE_CONTENT_EMPTY_ERROR);
        }
        if (article.getTags().isEmpty()) {
            throw new SystemException(ResultCodeEnum.ARTICLE_TAG_EMPTY_ERROR);
        }
        if (article.getCategoryId() == null) {
            throw new SystemException(ResultCodeEnum.ARTICLE_CATEGORY_EMPTY_ERROR);
        }

        RLock rLock = redisson.getLock(article.getCategoryId() + "Lock");
        List<RLock> rLockList = new ArrayList<>();
        for (Long tagId : article.getTags()) {
            RLock rLock1 = redisson.getLock(tagId + "Lock");
            rLockList.add(rLock1);
        }
        try {
            rLock.lock();
            for (RLock lock : rLockList) {
                lock.lock();
            }

            // 添加前检查分类是否被删除
            CategoryCheckVo categoryCheckVo = categoryMapper.selectStatusAndDeleteFlagById(article.getCategoryId());
            if (!"0".equals(categoryCheckVo.getStatus()) || !"0".equals(categoryCheckVo.getDelFlag())) {
                throw new SystemException(ResultCodeEnum.CATEGORY_ILLEGAL_ERROR);
            }
            // 检查标签是否被删除
            if (tagMapper.selectDisableTagCountByIds(article.getTags()) > 0) {
                throw new SystemException(ResultCodeEnum.TAG_ILLEGAL);
            }

            // 封装更新信息
            article.setUpdateBy(AuthenticationUtil.getUserFromContextHolder().getId());
            article.setUpdateTime(new Date());
            article.setCategoryName(categoryMapper.selectCategoryNameById(article.getCategoryId()));

            // 数据库更新文章
            articleMapper.updateArticle(article);

            List<Long> tags = article.getTags();
            // 删除更新后标签和文章的关系
            articleTagMapper.deleteByArticleId(article.getId());
            // 添加新的关系
            if (!tags.isEmpty()) {
                articleTagMapper.insertIntoArticleTag(article.getId(), tags);
            }

            // 删除文章缓存zset前，更新数据库浏览量
            updateViewCountSchedule.updateViewCount();

            RLock rLock1 = redisson.getLock(ARTICLE_ZSET_DELETE_LOCK);
            try {
                rLock1.lock();
                // 删除缓存文章zet and 删除分类缓存
                redisTemplate.delete(List.of(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), RedisKeyEnum.CATEGORY_ZSET_KEY.getKey()));

                // 创建新的缓存文章zet
                getPublishedArticleListFromCacheByCategoryId();

            } finally {
                if (rLock1.isHeldByCurrentThread()) {
                    rLock1.unlock();
                }
            }

        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
            for (RLock lock : rLockList) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return ResponseResult.ok(null);
    }


    /**
     * 删除文章
     * @param id
     * @return
     */
    @Transactional
    @Override
    public ResponseResult<Void> deleteArticle(Long id) {

        // 数据库逻辑删除文章
        articleMapper.deleteArticleById(id);
        //数据库逻辑删除文章评论
        commentMapper.deleteByArticleId(id);

        // 删除标签和文章的关系
        articleTagMapper.deleteByArticleId(id);

        // 缓存删除文章

        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {

                // 缓存文章zset删除该文章id
                redisTemplate.opsForZSet().remove(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), id);
                // 缓存删除文章详情
                redisTemplate.opsForHash().delete(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(),  RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id);

                // 缓存删除文章评论zset
                redisTemplate.delete(RedisKeyEnum.ARTICLE_ROOT_COMMENT_ZSET_KEY_PREFIX.getKey() + id);
                redisTemplate.delete(RedisKeyEnum.ARTICLE_CHILD_COMMENT_ZSET_KEY_PREFIX.getKey() + id);

                // 删除分类缓存
                redisTemplate.delete(RedisKeyEnum.CATEGORY_ZSET_KEY.getKey());
                return null;
            }
        });

        return ResponseResult.ok(null);
    }
}