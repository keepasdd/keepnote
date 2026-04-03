package com.keepasd.knowledgebase.dto.request;


import lombok.Data;

@Data
public class UpdateNoteDTO {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
}
