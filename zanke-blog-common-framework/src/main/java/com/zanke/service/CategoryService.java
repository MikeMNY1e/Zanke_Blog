package com.zanke.service;

import com.zanke.pojo.dto.CategoryListDto;
import com.zanke.pojo.entity.Category;
import com.zanke.pojo.vo.category.CategoryVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.util.ResponseResult;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface CategoryService {

    /**
     * 获取有有文章的分类
     * @return
     */
    ResponseResult<List<CategoryVo>> getCategoryList();


    /**
     * 获取所有启用的分类
     * @return
     */
    public ResponseResult<List<CategoryVo>> getEnableCategoryList();


    /**
     * 获取所有分类(导出分类excel用到)
     * @return
     */
    List<CategoryVo> getAllCategoryList();


    /**
     * 获取所有分类
     * @param categoryListDto
     * @return
     */
    ResponseResult<PageVo<List<CategoryVo>>> getCategoryListForAdmin(int pageNum, int pageSize, CategoryListDto categoryListDto);


    /**
     * 添加分类
     * @param category
     * @return
     */
    ResponseResult<Void> addCategory(Category category);


    /**
     * 获取分类详情
     * @param id
     * @return
     */
    ResponseResult<CategoryVo> getCategoryDetailById(Long id);


    /**
     * 修改分类
     * @param category
     * @return
     */
    ResponseResult<Void> updateCategory(Category category);


    /**
     * 删除分类
     * @param id
     * @return
     */
    ResponseResult<Void> deleteCategory(Long id);
}
