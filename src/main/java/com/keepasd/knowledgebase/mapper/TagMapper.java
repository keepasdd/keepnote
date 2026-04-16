package com.keepasd.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keepasd.knowledgebase.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 查询指定用户的所有标签（平铺列表，含 parentId）
     */
    List<Tag> selectByUserId(Long userId);
}

