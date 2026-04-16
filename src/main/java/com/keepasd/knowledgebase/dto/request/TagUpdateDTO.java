package com.keepasd.knowledgebase.dto.request;

import lombok.Data;

@Data
public class TagUpdateDTO {
    private Long id;
    private String name;
    private String color;
}
