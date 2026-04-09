package com.keepasd.knowledgebase.service.impl;

import com.keepasd.knowledgebase.service.FileService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private COSClient cosClient;

    @Value("${tencent.cos.bucket}")
    private String bucket;

    @Value("${tencent.cos.region}")
    private String region;

    @Override
    public String uploadImage(MultipartFile file) {
        return upload(file, "images/");
    }

    @Override
    public String uploadAttachment(MultipartFile file) {
        return upload(file, "attachments/");
    }

    private String upload(MultipartFile file, String prefix) {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String key = prefix + UUID.randomUUID() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            PutObjectRequest request = new PutObjectRequest(bucket, key, file.getInputStream(), metadata);
            cosClient.putObject(request);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        return "https://" + bucket + ".cos." + region + ".myqcloud.com/" + key;
    }
}
