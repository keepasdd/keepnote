package com.keepasd.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keepasd.knowledgebase.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT c.*, COUNT(n.id) AS note_count " +
            "FROM category c " +
            "LEFT JOIN note n ON n.category_id = c.id " +
            "WHERE c.user_id = #{userId} " +
            "GROUP BY c.id")
    List<Category> selectWithNoteCount(Long userId);
}
