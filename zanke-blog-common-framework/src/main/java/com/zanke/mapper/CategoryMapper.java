package com.zanke.mapper;

import com.zanke.pojo.dto.CategoryListDto;
import com.zanke.pojo.entity.Category;
import com.zanke.pojo.vo.category.CategoryCheckVo;
import com.zanke.pojo.vo.category.CategoryVo;

import java.util.Collection;
import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
public interface CategoryMapper {

    /**
     * 获取分类列表
     * @return
     */
    List<Category> selectCategoryList(Collection<Long> idSet, boolean all);


    /**
     * 根据id获取分类(修改文章用)
     * @param id
     * @return
     */
    Category selectCategoryByIdForAdmin(Long id);


    /**
     * 根据条件获取分类列表(后台)
     * @param categoryListDto
     * @return
     */
    List<CategoryVo> selectAllCategoryListByCategoryAndStatus(CategoryListDto categoryListDto);


    /**
     * 添加分类
     * @param category
     * @return
     */
    int insert(Category category);


    /**
     * 获取所有启用的分类
     * @return
     */
    List<CategoryVo> selectEnableCategoryList();


    /**
     * 修改分类
     * @param category
     * @return
     */
    int updateCategory(Category category);


    /**
     * 删除分类
     * @param id
     * @return
     */
    int deleteCategoryById(Long id);


    /**
     * 根据id获取分类状态和删除标记
     * @param categoryId
     * @return
     */
    CategoryCheckVo selectStatusAndDeleteFlagById(Long categoryId);


    /**
     * 根据id获取分类名称
     * @param categoryId
     * @return
     */
    String selectCategoryNameById(Long categoryId);
}
