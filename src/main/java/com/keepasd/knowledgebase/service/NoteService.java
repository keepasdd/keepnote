package com.keepasd.knowledgebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keepasd.knowledgebase.common.PageResult;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.entity.Note;

public interface NoteService extends IService<Note>{
    //新增笔记
    boolean addNote(Note note);
    //笔记的分页查询
    PageResult pageQuery(NoteQueryDTO noteQueryDTO);

    //查询笔记详细
    Note getbyId(Long id);

    //编辑笔记
    void updateNote(UpdateNoteDTO updateNoteDTO);
}
