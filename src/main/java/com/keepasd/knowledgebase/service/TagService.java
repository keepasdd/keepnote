package com.keepasd.knowledgebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
import com.keepasd.knowledgebase.entity.Tag;

public interface TagService extends IService<Tag> {
    void addTag(TagAddDTO tagAddDTO);
}
