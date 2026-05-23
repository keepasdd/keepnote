package com.keepasd.knowledgebase.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "keepnote_notes", createIndex = false)
public class NoteDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String content;

    @Field(type = FieldType.Integer)
    private Integer isFavorite;

    @Field(type = FieldType.Integer)
    private Integer isPinned;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
