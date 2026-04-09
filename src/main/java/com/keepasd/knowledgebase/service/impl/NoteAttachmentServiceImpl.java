package com.keepasd.knowledgebase.service.impl;

import com.keepasd.knowledgebase.entity.NoteAttachment;
import com.keepasd.knowledgebase.mapper.NoteAttachmentMapper;
import com.keepasd.knowledgebase.service.FileService;
import com.keepasd.knowledgebase.service.NoteAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteAttachmentServiceImpl implements NoteAttachmentService {

    @Autowired
    private FileService fileService;

    @Autowired
    private NoteAttachmentMapper noteAttachmentMapper;

    @Override
    public NoteAttachment upload(Long noteId, MultipartFile file) {
        String url = fileService.uploadAttachment(file);
        NoteAttachment attachment = new NoteAttachment();
        attachment.setNoteId(noteId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileSize(file.getSize());
        attachment.setUrl(url);
        attachment.setCreateTime(LocalDateTime.now());
        noteAttachmentMapper.insert(attachment);
        return attachment;
    }

    @Override
    public void delete(Long id) {
        noteAttachmentMapper.deleteById(id);
    }

    @Override
    public List<NoteAttachment> listByNoteId(Long noteId) {
        return noteAttachmentMapper.findByNoteId(noteId);
    }
}
