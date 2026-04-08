package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.AddCategoryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateCategoryDTO;
import com.keepasd.knowledgebase.entity.Category;
import com.keepasd.knowledgebase.service.CategoryService;
import com.keepasd.knowledgebase.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    //查看我的分类列表
    @GetMapping("/list")
    public Result getMyCategoryList() {
        log.info("用户{}正在获取我的分类列表", UserContext.getUserId());
        List<Category> list = categoryService.getMyCategoryList();
        return Result.success(list);
    }
    //新增分类
    @PostMapping("/add")
    public Result addCategory(@RequestBody AddCategoryDTO categoryDTO){
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    //修改分类
    @PutMapping("/update")
    public Result updateCategory(@RequestBody UpdateCategoryDTO categoryDTO){
        log.info("用户{}修改分类{}",UserContext.getUserId(),categoryDTO.getId());
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    //删除分类
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Long id){
        log.info("用户删除分类");
        categoryService.deleteCategoryById(id);
        return Result.success();
    }
}
