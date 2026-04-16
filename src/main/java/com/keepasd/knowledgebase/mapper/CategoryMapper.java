package com.keepasd.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keepasd.knowledgebase.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询当前用户所有分类，并统计每个分类下的笔记数
     * SQL 定义在 CategoryMapper.xml 中（resultMap 避免 JPA @Entity 干扰字段映射）
     */
    List<Category> selectWithNoteCount(@Param("userId") Long userId);
}
