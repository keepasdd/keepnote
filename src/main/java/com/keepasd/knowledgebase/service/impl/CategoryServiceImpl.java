package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keepasd.knowledgebase.dto.request.AddCategoryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateCategoryDTO;
import com.keepasd.knowledgebase.entity.Category;
import com.keepasd.knowledgebase.mapper.CategoryMapper;
import com.keepasd.knowledgebase.service.CategoryService;
import com.keepasd.knowledgebase.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getMyCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, UserContext.getUserId());
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public void addCategory(AddCategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setUserId(UserContext.getUserId());
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(UpdateCategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryMapper.deleteById(id);
    }
}
