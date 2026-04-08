package com.keepasd.knowledgebase.dto.request;


import lombok.Data;

import java.util.List;

@Data
public class UpdateNoteDTO {
    private Long id;
    private String title;
    private String content;
    private Long categoryId;
    private List<Integer> tagsIds;
    private Integer isFavorite;
}
