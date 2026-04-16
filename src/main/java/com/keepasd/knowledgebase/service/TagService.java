package com.keepasd.knowledgebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
import com.keepasd.knowledgebase.dto.request.TagUpdateDTO;
import com.keepasd.knowledgebase.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    /** 新增标签（支持 parentId，可选） */
    void addTag(TagAddDTO tagAddDTO);

    /** 修改标签名称 / 颜色 */
    void updateTag(TagUpdateDTO tagUpdateDTO);

    /** 返回当前用户标签的树形结构 */
    List<Tag> getTagTreeByUser();
}
