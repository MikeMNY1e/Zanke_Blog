package com.zanke.util;

import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public class RedisUtil {

    /**
     * 获取批量添加到ZSet的set
     *
     * @param itemScoreMap //     * @param clazz 存入的数据类型的class
     * @param <T>          存入的数据类型
     * @return
     * @throws Exception
     */
    public static <T> Set<ZSetOperations.TypedTuple<T>> getSetForZSetBatchAdd(Map<T, Double> itemScoreMap) throws Exception {

        Set<ZSetOperations.TypedTuple<T>> collect;

        collect = itemScoreMap.entrySet().stream()
                .map(entry ->
                        new DefaultTypedTuple<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());

        return collect;
    }
}
