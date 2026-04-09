package com.keepasd.knowledgebase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("note_attachment")
public class NoteAttachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private String fileName;
    private Long fileSize;
    private String url;
    private LocalDateTime createTime;
}
