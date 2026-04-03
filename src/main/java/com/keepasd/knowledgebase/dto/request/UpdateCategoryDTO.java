package com.keepasd.knowledgebase.dto.request;

import lombok.Data;

@Data
public class UpdateCategoryDTO {
    private Long id;
    private String name;
    private String color;
}
