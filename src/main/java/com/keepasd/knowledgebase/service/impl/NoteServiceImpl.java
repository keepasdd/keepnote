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
import com.keepasd.knowledgebase.util.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

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
    }

    @Override
    public PageResult pageQuery(NoteQueryDTO noteQueryDTO) {
        System.out.println("查询参数：" + noteQueryDTO); // 加这一行
        noteQueryDTO.setUserId(UserContext.getUserId());
        PageHelper.startPage(noteQueryDTO.getPage(), noteQueryDTO.getPageSize());
        Page<Note> page = noteMapper.pageQuery(noteQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
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
        if (!CollectionUtils.isEmpty(updateNoteDTO.getTagsIds())) {
            noteMapper.insertNoteTags(updateNoteDTO.getId(), updateNoteDTO.getTagsIds());
        }
    }
}
