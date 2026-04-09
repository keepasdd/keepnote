package com.keepasd.knowledgebase.service;

import com.keepasd.knowledgebase.entity.NoteAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NoteAttachmentService {
    NoteAttachment upload(Long noteId, MultipartFile file);
    void delete(Long id);
    List<NoteAttachment> listByNoteId(Long noteId);
}
