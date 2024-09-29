package com.zanke.controller;

import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.vo.category.CategoryVo;
import com.zanke.service.CategoryService;
import com.zanke.util.ResponseResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;


    /**
     * 获取分类列表
     * @return
     */
    @GetMapping("/getCategoryList")
    @UrlLogBusiness("获取分类列表")
    public ResponseResult<List<CategoryVo>> getCategoryList() {
        return categoryService.getCategoryList();
    }
}
