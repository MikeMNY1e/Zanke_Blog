package com.zanke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zanke.exception.SystemException;
import com.zanke.mapper.CommentMapper;
import com.zanke.mapper.UserMapper;
import com.zanke.pojo.entity.Comment;
import com.zanke.pojo.entity.User;
import com.zanke.pojo.vo.CommentVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.CommentService;
import com.zanke.util.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Slf4j
@Service("commentService")
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Comment> redisTemplate;
    @Resource
    private RedissonClient redisson;

    /**
     * 文章评论
     */
    private final String ARTICLE_TYPE = "0";
    /**
     * 友链评论
     */
    private final String LINK_TYPE = "1";


    /**
     * 发表评论
     *
     * @param comment
     * @return
     */
    @Override
    public ResponseResult<Void> addOneComment(Comment comment) {

        if (!StringUtils.hasText(comment.getContent())) {
            throw new SystemException(ResultCodeEnum.COMMENT_EMPTY_ERROR);
        }

        // 获取当前登录用户信息
        User user = AuthenticationUtil.getUserFromContextHolder();

        comment.setCreateBy(user.getId());
        comment.setCreateTime(new Date());
        comment.setUsername(user.getNickName());
        comment.setToCommentUserName(userMapper.selectNickNameById(comment.getToCommentUserId()));
        System.out.println(comment.getCreateTime());
        System.out.println(comment.getCreateTime().getTime());
        // 加入数据库
        int affectedRows = commentMapper.insertOneComment(comment);

        if (affectedRows > 0) {

            try {

                String rootKey;
                String childKey;
                if ("0".equals(comment.getType())) {
                    rootKey = RedisKeyEnum.ARTICLE_ROOT_COMMENT_ZSET_KEY_PREFIX.getKey() + comment.getArticleId();
                    childKey = RedisKeyEnum.ARTICLE_CHILD_COMMENT_ZSET_KEY_PREFIX.getKey() + comment.getArticleId();
                } else {
                    rootKey = RedisKeyEnum.LINK_ROOT_COMMENT_ZSET_KEY.getKey();
                    childKey = RedisKeyEnum.LINK_CHILD_COMMENT_ZSET_KEY.getKey();
                }

                // 添加到缓存
                if (comment.getRootId() == -1) {
                    //如果是添加根评论
                    redisTemplate.opsForZSet().add(rootKey, comment, -1d * comment.getCreateTime().getTime());
                } else {
                    // 如果添加的子评论
                    redisTemplate.opsForZSet().add(childKey, comment, -1d * comment.getCreateTime().getTime());
                }

            } catch (Exception e) {
                log.error("缓存添加评论异常", e);
                throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
            }
            return ResponseResult.ok(null);

        } else {
            throw new SystemException(ResultCodeEnum.COMMENT_ADD_ERROR);
        }
    }


    /**
     * 根据文章id查询评论列表
     * <p>
     * 策略：根据每个文章区分，每个文章一个所有根评论集合，和一个所有子评论集合，业务上for遍历整合返回格式
     * <p>
     * 其它想法：每个文章一个所有根评论集合，每个根评论一个子评论集合
     *
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<CommentVo>>> getCommentListByArticleId(Long articleId, int pageNum, int pageSize) {

        return getCommentList(RedisKeyEnum.ARTICLE_ROOT_COMMENT_ZSET_KEY_PREFIX.getKey() + articleId, RedisKeyEnum.ARTICLE_CHILD_COMMENT_ZSET_KEY_PREFIX.getKey() + articleId, articleId, pageNum, pageSize, ARTICLE_TYPE);
    }


    /**
     * 获取友链评论
     *
     * @param articleId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ResponseResult<PageVo<List<CommentVo>>> getLinkCommentList(Long articleId, int pageNum, int pageSize) {

        return getCommentList(RedisKeyEnum.LINK_ROOT_COMMENT_ZSET_KEY.getKey(), RedisKeyEnum.LINK_CHILD_COMMENT_ZSET_KEY.getKey(), articleId, pageNum, pageSize, LINK_TYPE);
    }


    private ResponseResult<PageVo<List<CommentVo>>> getCommentList(String rootKey, String childKey, Long articleId, int pageNum, int pageSize, String type) {

        Long total = 0L;
        int pageStartIndex = (pageNum - 1) * pageSize;
        int pageEndIndex = pageStartIndex + pageSize - 1;

        Set<Comment> rootCommentSet;
        List<Comment> rootCommentList = new ArrayList<>();

        // 缓存查询该文章的根评论
        try {
            rootCommentSet = redisTemplate.opsForZSet().range(rootKey, pageStartIndex, pageEndIndex);
            if (rootCommentSet == null) {
                rootCommentSet = Collections.emptySet();
            }
            if (!rootCommentSet.isEmpty()) {
                total = redisTemplate.opsForZSet().size(rootKey);
            }
        } catch (Exception e) {
            throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
        }

        if (rootCommentSet.isEmpty()) {

            RLock rootLock = redisson.getLock(rootKey + "Lock");
            try {
                rootLock.lock();

                // 再次查询缓存
                try {
                    rootCommentSet = redisTemplate.opsForZSet().range(rootKey, pageStartIndex, pageEndIndex);
                    if (rootCommentSet == null) {
                        rootCommentSet = Collections.emptySet();
                    }
                    if (!rootCommentSet.isEmpty()) {
                        total = redisTemplate.opsForZSet().size(rootKey);
                    }
                } catch (Exception e) {
                    throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                }

                if (rootCommentSet.isEmpty()) {
                    // 查数据库

                    try {
                        PageHelper.startPage(pageNum, pageSize);
                        rootCommentList = commentMapper.selectRootCommentListByArticleIdDESCBYCreateTime(articleId, type);
                        total = new PageInfo<>(rootCommentList).getTotal();
                        rootCommentSet = new LinkedHashSet<>(rootCommentList);

                    } catch (Exception e) {
                        log.error("数据库根据文章id查询评论列表异常", e);
                        throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                    } finally {
                        PageHelper.clearPage();
                    }

                    try {
                        // 写入缓存
                        Map<Comment, Double> map = rootCommentSet.stream()
                                .collect(Collectors.toMap(comment -> comment, comment -> -1d * comment.getCreateTime().getTime()));

                        Set<ZSetOperations.TypedTuple<Comment>> collect = RedisUtil.getSetForZSetBatchAdd(map);

                        if (collect.isEmpty()) {
                            // 如果数据库没有，则写回空，设置过期时间
                            String luaScript = """
                                    local function writeCommentIfEmpty()
                                        redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2])
                                        redis.call('EXPIRE', KEYS[1], ARGV[3])
                                    end
                                    return writeCommentIfEmpty()
                                    """;
                            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                                    List.of(rootKey),
                                    8d,
                                    new Comment(),
                                    30);
                        } else {
                            redisTemplate.opsForZSet().add(rootKey, collect);
                        }

                    } catch (Exception e) {
                        log.error("缓存写入根评论集合异常", e);
                        throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                    }
                }

            } finally {
                if (rootLock.isHeldByCurrentThread()) {
                    rootLock.unlock();
                }
            }
        }

        Set<Comment> childCommentSet;

        // 缓存查询该文章的子评论
        try {
            childCommentSet = redisTemplate.opsForZSet().range(childKey, 0, -1);
            if (childCommentSet == null) {
                childCommentSet = Collections.emptySet();
            }
        } catch (Exception e) {
            throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
        }

        if (childCommentSet.isEmpty()) {
            RLock childLock = redisson.getLock(childKey + "Lock");
            try {
                childLock.lock();

                // 再次查询缓存
                try {
                    childCommentSet = redisTemplate.opsForZSet().range(childKey, 0, -1);
                    if (childCommentSet == null) {
                        childCommentSet = Collections.emptySet();
                    }
                } catch (Exception e) {
                    throw new SystemException(ResultCodeEnum.SYSTEM_ERROR);
                }
                if (childCommentSet.isEmpty()) {
                    // 查数据库
                    List<Comment> childCommentList = new ArrayList<>();
                    try {
                        Set<Long> rootIds = rootCommentList.stream().map(Comment::getId).collect(Collectors.toSet());
                        if (rootIds.isEmpty()) {
                            rootIds.add(-1L);
                        }
                        childCommentList = commentMapper.selectChildCommentListByRootIdDESCBYCreateTime(rootIds);
                        childCommentSet = new LinkedHashSet<>(childCommentList);

                    } catch (Exception e) {
                        log.error("数据库根据根评论id查询子评论列表异常", e);
                    }

                    try {
                        // 写入缓存
                        Map<Comment, Double> map = childCommentList.stream()
                                .collect(Collectors.toMap(comment -> comment, comment -> -1d * comment.getCreateTime().getTime()));

                        Set<ZSetOperations.TypedTuple<Comment>> collect = RedisUtil.getSetForZSetBatchAdd(map);

                        if (collect.isEmpty()) {
                            // 如果数据库没有，则写回空，设置过期时间
                            String luaScript = """
                                    local function writeCommentIfEmpty()
                                        redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2])
                                        redis.call('EXPIRE', KEYS[1], ARGV[3])
                                    end
                                    return writeCommentIfEmpty()
                                    """;
                            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                                    List.of(childKey),
                                    8d,
                                    new Comment(),
                                    30);
                        } else {
                            redisTemplate.opsForZSet().add(childKey, collect);
                        }

                    } catch (Exception e) {
                        log.error("缓存写入子评论集合异常", e);
                    }
                }

            } finally {
                if (childLock.isHeldByCurrentThread()) {
                    childLock.unlock();
                }
            }
        }

        List<CommentVo> commentVoList = convertToCommentVoList(rootCommentSet, childCommentSet);

        return ResponseResult.ok(new PageVo<>(total, commentVoList));
    }

    /**
     * 将根评论和子评论转换为CommentVo
     * @param rootCommentSet
     * @param childCommentSet
     * @return
     */
    private List<CommentVo> convertToCommentVoList(Set<Comment> rootCommentSet, Set<Comment> childCommentSet) {

        List<CommentVo> rootCommentVoList = BeanCopyUtil.copyBeanCollectionToList(rootCommentSet, CommentVo.class);
        List<CommentVo> childCommentVoList = BeanCopyUtil.copyBeanCollectionToList(childCommentSet, CommentVo.class);

        ArrayList<CommentVo> commentVoList = new ArrayList<>();
        for (CommentVo rootComment : rootCommentVoList) {

            if (rootComment.getId() != null) {
                ArrayList<CommentVo> childOfRootList = new ArrayList<>();

                for (CommentVo childComment : childCommentVoList) {

                    if (rootComment.getId().equals(childComment.getRootId())) {
                        // 找到子评论，添加到父评论的children中
                        childOfRootList.add(childComment);
                    }
                }

                rootComment.setChildren(childOfRootList);
                commentVoList.add(rootComment);
            }
        }

        return commentVoList;
    }
}