package com.keepasd.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keepasd.knowledgebase.entity.NoteAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface NoteAttachmentMapper extends BaseMapper<NoteAttachment> {

    @Select("SELECT * FROM note_attachment WHERE note_id = #{noteId}")
    List<NoteAttachment> findByNoteId(Long noteId);

    @Delete("DELETE FROM note_attachment WHERE note_id = #{noteId}")
    void deleteByNoteId(Long noteId);
}
