package com.keepasd.knowledgebase.controller;

import com.keepasd.knowledgebase.common.Result;
import com.keepasd.knowledgebase.dto.request.NoteCreateDTO;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.dto.response.NoteDetailVO;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.entity.NoteAttachment;
import com.keepasd.knowledgebase.service.NoteAttachmentService;
import com.keepasd.knowledgebase.service.NoteService;
import com.keepasd.knowledgebase.service.UserService;
import com.keepasd.knowledgebase.util.UserContext;
import org.springframework.beans.BeanUtils;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/note")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private NoteAttachmentService noteAttachmentService;

    @PostMapping("/add")
    public Result add(@RequestBody NoteCreateDTO noteCreateDTO) {
        log.info("新增笔记，userId={}, dto={}", UserContext.getUserId(), noteCreateDTO);
        noteService.addNote(noteCreateDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result listforNote(NoteQueryDTO noteQueryDTO) {
        log.info("分页查询笔记，params={}", noteQueryDTO);
        return Result.success(noteService.pageQuery(noteQueryDTO));
    }

    @GetMapping("/{id}")
    public Result<NoteDetailVO> getById(@PathVariable Long id) {
        log.info("查询笔记详情，id={}", id);
        Note note = noteService.getbyId(id);
        if (note == null) {
            return Result.fail("Note not found");
        }
        NoteDetailVO vo = new NoteDetailVO();
        BeanUtils.copyProperties(note, vo);
        vo.setAttachments(noteAttachmentService.listByNoteId(id));
        return Result.success(vo);
    }

    @PutMapping("/update")
    public Result updateNote(@RequestBody UpdateNoteDTO updateNoteDTO) {
        log.info("编辑笔记，id={}", updateNoteDTO.getId());
        noteService.updateNote(updateNoteDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteNote(@PathVariable Long id) {
        log.info("删除笔记请求，id={}, userId={}", id, UserContext.getUserId());
        Note note = noteService.getbyId(id);
        if (note == null)
            return Result.fail("无此笔记！");
        if (!note.getUserId().equals(UserContext.getUserId()))
            return Result.fail("您无权删除别人的笔记！");
        boolean b = noteService.removeById(id);
        log.info("删除笔记{}，id={}", b ? "成功" : "失败", id);
        return b ? Result.success() : Result.fail("删除失败！");
    }
}
