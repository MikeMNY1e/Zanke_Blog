package com.zanke.util;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zanke
 * @version 1.0.0
 * @description BeanCopy工具类
 */
public class BeanCopyUtil {

    private BeanCopyUtil() {
    }

    public static <V> V copyBean(Object source,Class<V> clazz) {
        //创建目标对象
        V result = null;
        try {
            //反射创建对象
            Constructor<V> constructor = clazz.getConstructor();
            result = constructor.newInstance();
            //实现属性copy
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回结果
        return result;
    }


    /**
     * List接口集合的批量copy操作
     * @param collection
     * @param clazz
     * @return
     * @param <O>
     * @param <V>
     */
    public static <O,V> List<V> copyBeanCollectionToList(Collection<O> collection, Class<V> clazz){
        return collection.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }
}
