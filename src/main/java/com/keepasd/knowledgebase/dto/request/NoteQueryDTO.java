package com.keepasd.knowledgebase.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteQueryDTO {
    //页码
    private Integer page;
    //每页记录数
    private Integer pageSize;
    //搜索关键词
    private String keyword;
    //种类ID
    private Integer categoryId;
    //标签ID
    private Integer tagId;
    //是否喜欢
    private Integer isFavorite;
    //数据范围
    private String dateRange;

}
