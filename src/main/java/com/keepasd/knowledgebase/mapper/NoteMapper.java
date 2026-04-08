package com.keepasd.knowledgebase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.entity.Note;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    Page<Note> pageQuery(NoteQueryDTO noteQueryDTO);

    Note getById(Long id);

    void insertNoteTags(@Param("noteId") Long noteId, @Param("tagIds") List<Integer> tagIds);

    @Delete("DELETE FROM note_tag WHERE note_id = #{noteId}")
    void deleteNoteTags(Long noteId);
}
