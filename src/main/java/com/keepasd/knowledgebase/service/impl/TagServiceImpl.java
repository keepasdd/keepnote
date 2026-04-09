package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
import com.keepasd.knowledgebase.entity.Tag;
import com.keepasd.knowledgebase.mapper.TagMapper;
import com.keepasd.knowledgebase.service.TagService;
import com.keepasd.knowledgebase.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Override
    public void addTag(TagAddDTO tagAddDTO) {
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddDTO,tag);
        tag.setUserId(UserContext.getUserId());
        tagMapper.insert(tag);
    }
}
