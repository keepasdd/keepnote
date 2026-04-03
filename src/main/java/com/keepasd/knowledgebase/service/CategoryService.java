package com.keepasd.knowledgebase.service;

import com.keepasd.knowledgebase.dto.request.AddCategoryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateCategoryDTO;
import com.keepasd.knowledgebase.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getMyCategoryList();

    void addCategory(AddCategoryDTO categoryDTO);

    void updateCategory(UpdateCategoryDTO categoryDTO);

    void deleteCategoryById(Long id);
}
