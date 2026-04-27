package com.keepasd.knowledgebase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keepasd.knowledgebase.common.PageResult;
import com.keepasd.knowledgebase.dto.request.NoteCreateDTO;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.entity.Note;

public interface NoteService extends IService<Note> {
    Long addNote(NoteCreateDTO noteCreateDTO);
    PageResult pageQuery(NoteQueryDTO noteQueryDTO);
    Note getbyId(Long id);
    void updateNote(UpdateNoteDTO updateNoteDTO);
    void pinNote(Long id);
}
