package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
import com.keepasd.knowledgebase.dto.request.TagUpdateDTO;
import com.keepasd.knowledgebase.entity.Tag;
import com.keepasd.knowledgebase.service.TagService;
import com.keepasd.knowledgebase.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tag")
public class TagController {
    @Autowired
    private TagService tagService;

    // 获取标签树形列表（多级嵌套）
    @GetMapping("/list")
    public Result getTagsList(){
        log.info("用户{}尝试获取标签树形列表", UserContext.getUserId());
        List<Tag> tree = tagService.getTagTreeByUser();
        log.info("用户获取标签树形列表成功，根节点数={}", tree.size());
        return Result.success(tree);
    }

    // 新增标签（parentId 可选，为 null 则创建根标签）
    @PostMapping("/add")
    public Result addTag(@RequestBody TagAddDTO tagAddDTO){
        tagService.addTag(tagAddDTO);
        log.info("用户新增标签{}", tagAddDTO);
        return Result.success();
    }

    // 修改标签（name / color）
    @PutMapping("/update")
    public Result updateTag(@RequestBody TagUpdateDTO tagUpdateDTO){
        tagService.updateTag(tagUpdateDTO);
        log.info("用户修改标签{}", tagUpdateDTO);
        return Result.success();
    }

    // 删除标签（数据库 ON DELETE CASCADE 级联删除子标签）
    @DeleteMapping("/{id}")
    public Result deleteTag(@PathVariable Long id){
        tagService.removeById(id);
        log.info("用户删除标签ID={}", id);
        return Result.success();
    }
}
