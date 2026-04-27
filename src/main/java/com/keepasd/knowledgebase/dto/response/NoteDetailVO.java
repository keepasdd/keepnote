package com.keepasd.knowledgebase.dto.response;

import com.keepasd.knowledgebase.entity.NoteAttachment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteDetailVO {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isFavorite;
    private Integer isPinned;
    private List<NoteAttachment> attachments;
}
