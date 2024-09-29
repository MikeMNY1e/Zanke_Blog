package com.zanke.controller;

import com.alibaba.excel.EasyExcel;
import com.zanke.annotation.UrlLogBusiness;
import com.zanke.pojo.dto.CategoryListDto;
import com.zanke.pojo.entity.Category;
import com.zanke.pojo.vo.category.CategoryVo;
import com.zanke.pojo.vo.category.ExcelCategoryVo;
import com.zanke.pojo.vo.PageVo;
import com.zanke.service.CategoryService;
import com.zanke.util.BeanCopyUtil;
import com.zanke.util.ResponseResult;
import com.zanke.util.ResultCodeEnum;
import com.zanke.util.WebUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@RestController
@RequestMapping("/content/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;


    @GetMapping("/listAllCategory")
    @UrlLogBusiness("获取所有启用分类(写文章用到)")
    public ResponseResult<List<CategoryVo>> listAllCategory() {
        return categoryService.getEnableCategoryList();
    }


    @PreAuthorize("@permission.hasPermission('content:category:export')")
    @GetMapping("/export")
    @UrlLogBusiness("导出分类")
    public void exportCategoryForm(HttpServletResponse httpServletResponse) {

        try {
            // 设置下载文件的请求头
            WebUtil.setDownLoadHeader("分类.xlsx", httpServletResponse);
            // 获取需要导出的数据
            List<CategoryVo> categoryVoList = categoryService.getAllCategoryList();
            List<ExcelCategoryVo> excelCategoryVoList = BeanCopyUtil.copyBeanCollectionToList(categoryVoList, ExcelCategoryVo.class);
            // 把数据写到excel中
            EasyExcel.write(httpServletResponse.getOutputStream(), ExcelCategoryVo.class)
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("分类导出")
                    .doWrite(excelCategoryVoList);

        } catch (Exception e) {
            ResponseResult<Void> responseResult = ResponseResult.build(null, ResultCodeEnum.SYSTEM_ERROR);
            WebUtil.responseJson(httpServletResponse, responseResult);
        }
    }


    @GetMapping("/list")
    @UrlLogBusiness("获取分类列表")
    public ResponseResult<PageVo<List<CategoryVo>>> getCategoryList(int pageNum, int pageSize, CategoryListDto categoryListDto) {
        return categoryService.getCategoryListForAdmin(pageNum, pageSize, categoryListDto);
    }


    @PostMapping
    @UrlLogBusiness("添加分类")
    public ResponseResult<Void> addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }


    @GetMapping("/{id}")
    @UrlLogBusiness("获取分类信息")
    public ResponseResult<CategoryVo> getCategoryInfoById(@PathVariable Long id) {
        return categoryService.getCategoryDetailById(id);
    }


    @PutMapping
    @UrlLogBusiness("修改分类")
    public ResponseResult<Void> updateCategory(@RequestBody Category category) {
        return categoryService.updateCategory(category);
    }


    @DeleteMapping("/{id}")
    @UrlLogBusiness("删除分类")
    public ResponseResult<Void> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}