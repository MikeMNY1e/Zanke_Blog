package com.zanke.util;

import lombok.Getter;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@Getter
public enum RedisKeyEnum {

    ALL_ARTICLE_ID_ZSET_KEY("articleIdZSet"),
    ARTICLE_DETAIL_HASH_KEY("articleDetailHash"),
    ARTICLE_DETAIL_HASH_FIELD_PREFIX("articleDetail:aid"),
    CATEGORY_ZSET_KEY("categoryZSet"),
    ARTICLE_ROOT_COMMENT_ZSET_KEY_PREFIX("rootCommentZSet:aid"),
    ARTICLE_CHILD_COMMENT_ZSET_KEY_PREFIX("childCommentZSet:aid"),
    LINK_ROOT_COMMENT_ZSET_KEY("rootCommentZSet:link"),
    LINK_CHILD_COMMENT_ZSET_KEY("childCommentZSet:link"),
    USERNAME_SET_KEY("usernameSet"),
    NICKNAME_SET_KEY("nickNameSet"),
    LOGINUSER_USERNAME_HASH_KEY("loginUserUsernameHash"),
    LOGINUSER_USERNAME_FIELD_PREFIX("username:"),
    LINK_SET_KEY("linkZSet"),
    ARTICLE_VIEWCOUNT_HLL_PREFIX("articleViewCountHll:aid");

    private String key;
    RedisKeyEnum (String key) {
        this.key = key;
    }
}
