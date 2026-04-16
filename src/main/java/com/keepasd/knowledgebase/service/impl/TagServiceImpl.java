package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
import com.keepasd.knowledgebase.dto.request.TagUpdateDTO;
import com.keepasd.knowledgebase.entity.Tag;
import com.keepasd.knowledgebase.mapper.TagMapper;
import com.keepasd.knowledgebase.service.TagService;
import com.keepasd.knowledgebase.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public void addTag(TagAddDTO tagAddDTO) {
        // 若传入了 parentId，校验该父标签是否属于当前用户
        if (tagAddDTO.getParentId() != null) {
            Tag parent = tagMapper.selectById(tagAddDTO.getParentId());
            if (parent == null || !parent.getUserId().equals(UserContext.getUserId())) {
                throw new IllegalArgumentException("父标签不存在或无权访问");
            }
        }
        Tag tag = new Tag();
        BeanUtils.copyProperties(tagAddDTO, tag);
        tag.setUserId(UserContext.getUserId());
        tagMapper.insert(tag);
    }

    @Override
    public void updateTag(TagUpdateDTO tagUpdateDTO) {
        Tag tag = new Tag();
        tag.setId(tagUpdateDTO.getId());
        tag.setName(tagUpdateDTO.getName());
        tag.setColor(tagUpdateDTO.getColor());
        tagMapper.updateById(tag);
    }

    @Override
    public List<Tag> getTagTreeByUser() {
        Long userId = UserContext.getUserId();
        // 1. 平铺查询当前用户所有标签
        List<Tag> all = tagMapper.selectByUserId(userId);

        // 2. 以 id 为 key 建 Map，并初始化 children 列表
        Map<Long, Tag> map = new HashMap<>();
        for (Tag t : all) {
            t.setChildren(new ArrayList<>());
            map.put(t.getId(), t);
        }

        // 3. 遍历，将子节点挂载到父节点
        List<Tag> roots = new ArrayList<>();
        for (Tag t : all) {
            if (t.getParentId() == null) {
                roots.add(t);
            } else {
                Tag parent = map.get(t.getParentId());
                if (parent != null) {
                    parent.getChildren().add(t);
                } else {
                    // 父标签不存在（数据异常），作为根节点兜底处理
                    roots.add(t);
                }
            }
        }
        return roots;
    }
}
