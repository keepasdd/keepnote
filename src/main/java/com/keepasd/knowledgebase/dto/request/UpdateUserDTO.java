package com.keepasd.knowledgebase.dto.request;

import lombok.Data;

@Data
public class UpdateUserDTO {
    private String nickname;
    private String email;
    private String avatar;
}
