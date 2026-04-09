package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.TagAddDTO;
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

    //获取标签列表
    @GetMapping("/list")
    public Result getTagsList(){
        log.info("用户{}尝试获取标签列表", UserContext.getUserId());
        List<Tag> list = tagService.list();
        log.info("用户获取标签列表成功{}",list);
        return Result.success(list);
    }

    //新增标签
    @PostMapping("/add")
    public Result addTag(@RequestBody TagAddDTO tagAddDTO){
        tagService.addTag(tagAddDTO);
        log.info("用户新增标签{}",tagAddDTO);
        return Result.success();
    }

    //删除标签
    @DeleteMapping("/{id}")
    public Result deletetag(@PathVariable Long id){
        tagService.removeById(id);
        log.info("用户删除标签ID{}",id);
        return Result.success();
    }
}
