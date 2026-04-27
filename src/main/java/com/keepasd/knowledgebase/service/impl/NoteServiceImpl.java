package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.keepasd.knowledgebase.common.PageResult;
import com.keepasd.knowledgebase.dto.request.NoteCreateDTO;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.mapper.NoteMapper;
import com.keepasd.knowledgebase.service.NoteService;
import com.keepasd.knowledgebase.util.RedisConstant;
import com.keepasd.knowledgebase.util.RedisUtil;
import com.keepasd.knowledgebase.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public void addNote(NoteCreateDTO noteCreateDTO) {
        Note note = new Note();
        BeanUtils.copyProperties(noteCreateDTO, note);
        note.setUserId(UserContext.getUserId());
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        noteMapper.insert(note);
        // 插入标签关联
        if (!CollectionUtils.isEmpty(noteCreateDTO.getTagIds())) {
            noteMapper.insertNoteTags(note.getId(), noteCreateDTO.getTagIds());
        }
        redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
    }

    @Override
    public PageResult pageQuery(NoteQueryDTO noteQueryDTO) {
        log.info("查询笔记列表，userId=" + UserContext.getUserId() + ", params=" + noteQueryDTO);
        noteQueryDTO.setUserId(UserContext.getUserId());
        //先在redis中查询笔记列表，如果存在则直接返回，否则从数据库中查询并将结果存入redis
        // Key 包含所有查询参数，避免不同条件命中同一份缓存
        String key = RedisConstant.NOTE_LIST_KEY + UserContext.getUserId()
                + ":" + noteQueryDTO.getPage()
                + ":" + noteQueryDTO.getPageSize()
                + ":" + noteQueryDTO.getKeyword()
                + ":" + noteQueryDTO.getCategoryId()
                + ":" + noteQueryDTO.getTagId()
                + ":" + noteQueryDTO.getIsFavorite()
                + ":" + noteQueryDTO.getDateRange();
        PageResult object = redisUtil.getObject(key, PageResult.class);
        if (object != null) {
            log.info("从缓存获取笔记列表成功，key={}", key);
            return object;
        }
        // redis中不存在，查询数据库
        PageHelper.startPage(noteQueryDTO.getPage(), noteQueryDTO.getPageSize());
        Page<Note> page = noteMapper.pageQuery(noteQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        // 将结果存入redis，设置过期时间为10分钟
        redisUtil.setObject(key, pageResult, RedisConstant.NOTE_LIST_TTL, TimeUnit.SECONDS);
        return pageResult;
    }

    @Override
    public Note getbyId(Long id) {
        return noteMapper.getById(id);
    }

    @Override
    @Transactional
    public void updateNote(UpdateNoteDTO updateNoteDTO) {
        Note note = new Note();
        note.setId(updateNoteDTO.getId());
        note.setTitle(updateNoteDTO.getTitle());
        note.setContent(updateNoteDTO.getContent());
        note.setCategoryId(updateNoteDTO.getCategoryId());
        note.setUpdateTime(LocalDateTime.now());
        note.setIsFavorite(updateNoteDTO.getIsFavorite());
        noteMapper.updateById(note);
        // 先删除旧标签关联，再插入新的
        noteMapper.deleteNoteTags(updateNoteDTO.getId());
        if (!CollectionUtils.isEmpty(updateNoteDTO.getTagIds())) {
            noteMapper.insertNoteTags(updateNoteDTO.getId(), updateNoteDTO.getTagIds());
        }
        redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
    }
}
