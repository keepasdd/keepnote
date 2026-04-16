package com.keepasd.knowledgebase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private String color;

    /** 父标签 ID，null 表示根标签 */
    private Long parentId;

    /** 子标签列表（非数据库字段，查询时在内存中组装） */
    @TableField(exist = false)
    private List<Tag> children = new ArrayList<>();
}
