package com.zanke.schedule;


import com.zanke.mapper.ArticleMapper;
import com.zanke.pojo.entity.Article;
import com.zanke.util.RedisKeyEnum;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Component
public class UpdateViewCountSchedule {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private RedisTemplate<String, Number> redisTemplate;


    /**
     * 每十分钟更新一次浏览量
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateViewCount(){

        // 获取所有文章id的zset
        Set<Number> articleIdSet = redisTemplate.opsForZSet().range(RedisKeyEnum.ALL_ARTICLE_ID_ZSET_KEY.getKey(), 0, -1);

        List<String> fieldList = articleIdSet.stream().map(id -> RedisKeyEnum.ARTICLE_DETAIL_HASH_FIELD_PREFIX.getKey() + id).toList();

        //获得所有文章的浏览量
        List<Article> articleList =
                redisTemplate.<String, Article>opsForHash().multiGet(RedisKeyEnum.ARTICLE_DETAIL_HASH_KEY.getKey(), fieldList);

        // 更新数据库中的浏览量
        articleList.forEach(article -> {
            articleMapper.updateViewCount(article.getId(), article.getViewCount());
        });
    }
}
