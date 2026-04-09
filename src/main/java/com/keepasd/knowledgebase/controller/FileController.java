package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.entity.NoteAttachment;
import com.keepasd.knowledgebase.service.FileService;
import com.keepasd.knowledgebase.service.NoteAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private NoteAttachmentService noteAttachmentService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String url = fileService.uploadImage(file);
        return Result.success(url);
    }

    @PostMapping("/attachment/upload")
    public Result<NoteAttachment> uploadAttachment(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("noteId") Long noteId) {
        NoteAttachment attachment = noteAttachmentService.upload(noteId, file);
        return Result.success(attachment);
    }

    @DeleteMapping("/attachment/{id}")
    public Result<?> deleteAttachment(@PathVariable Long id) {
        noteAttachmentService.delete(id);
        return Result.success();
    }
}
