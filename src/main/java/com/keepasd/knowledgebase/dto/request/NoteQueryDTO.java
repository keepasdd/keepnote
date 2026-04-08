package com.keepasd.knowledgebase.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoteQueryDTO {
    //页码
    private Integer page = 1;
    //每页记录数
    private Integer pageSize = 10;
    //搜索关键词
    private String keyword;
    //种类ID
    private Long categoryId;
    //标签ID
    private Long tagId;
    //是否喜欢
    private Integer isFavorite;
    //数据范围
    private String dateRange;
    //用户ID（内部使用，不由前端传入）
    private Long userId;

}
