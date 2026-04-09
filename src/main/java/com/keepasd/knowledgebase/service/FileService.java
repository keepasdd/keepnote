package com.keepasd.knowledgebase.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String uploadImage(MultipartFile file);
    String uploadAttachment(MultipartFile file);
}
