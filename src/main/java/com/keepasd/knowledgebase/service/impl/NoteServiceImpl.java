package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.keepasd.knowledgebase.common.PageResult;
import com.keepasd.knowledgebase.dto.request.NoteCreateDTO;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.mapper.NoteAttachmentMapper;
import com.keepasd.knowledgebase.mapper.NoteMapper;
import com.keepasd.knowledgebase.mq.NoteDeleteDelayProducer;
import com.keepasd.knowledgebase.mq.NoteIndexMessageProducer;
import com.keepasd.knowledgebase.service.NoteService;
import com.keepasd.knowledgebase.util.RedisConstant;
import com.keepasd.knowledgebase.util.RedisUtil;
import com.keepasd.knowledgebase.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private NoteIndexMessageProducer noteIndexMessageProducer;
    @Autowired
    private NoteDeleteDelayProducer noteDeleteDelayProducer;
    @Autowired
    private NoteAttachmentMapper noteAttachmentMapper;

    @Override
    @Transactional
    public Long addNote(NoteCreateDTO noteCreateDTO) {
        Note note = new Note();
        BeanUtils.copyProperties(noteCreateDTO, note);
        note.setUserId(UserContext.getUserId());
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        note.setIsDeleted(0);
        noteMapper.insert(note);
        redisUtil.setObject(RedisConstant.NOTE_DETAIL_KEY + note.getId(), note, RedisConstant.NOTE_LIST_TTL, TimeUnit.SECONDS);
        // 插入标签关联
        if (!CollectionUtils.isEmpty(noteCreateDTO.getTagIds())) {
            noteMapper.insertNoteTags(note.getId(), noteCreateDTO.getTagIds());
        }
        redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
        // 写库成功后发 RabbitMQ 消息，由消费者异步重建全文索引。
        noteIndexMessageProducer.sendAfterCommit(note.getId(), note.getUserId(), "CREATE");
        return note.getId();
    }

    @Override
    public PageResult pageQuery(NoteQueryDTO noteQueryDTO) {
        log.info("查询笔记列表，userId=" + UserContext.getUserId() + ", params=" + noteQueryDTO);
        noteQueryDTO.setUserId(UserContext.getUserId());
        if (noteQueryDTO.getIsDeleted() == null) {
            noteQueryDTO.setIsDeleted(0);
        }
        //先在redis中查询笔记列表，如果存在则直接返回，否则从数据库中查询并将结果存入redis
        // Key 包含所有查询参数，避免不同条件命中同一份缓存
        String key = RedisConstant.NOTE_LIST_KEY + UserContext.getUserId()
                + ":" + noteQueryDTO.getPage()
                + ":" + noteQueryDTO.getPageSize()
                + ":" + noteQueryDTO.getKeyword()
                + ":" + noteQueryDTO.getCategoryId()
                + ":" + noteQueryDTO.getTagId()
                + ":" + noteQueryDTO.getIsFavorite()
                + ":" + noteQueryDTO.getDateRange()
                + ":" + noteQueryDTO.getIsDeleted();
        PageResult object = redisUtil.getObject(key, PageResult.class);
        if (object != null) {
            log.info("从缓存获取笔记列表成功，key={}", key);
            return object;
        }
        // redis中不存在，查询数据库
        PageHelper.startPage(noteQueryDTO.getPage(), noteQueryDTO.getPageSize());
        Page<Note> page = noteMapper.pageQuery(noteQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        // 将结果存入redis，设置过期时间为10分钟
        redisUtil.setObject(key, pageResult, RedisConstant.NOTE_LIST_TTL, TimeUnit.SECONDS);
        return pageResult;
    }

    @Override
    public Note getbyId(Long id) {
        //先在redis里面去寻找
        Note object = redisUtil.getObject(RedisConstant.NOTE_DETAIL_KEY + id, Note.class);
        if (object != null) {
            log.info("从缓存获取笔记详情成功，id={}", id);
            return object;
        }
        //如果redis中没找到则在mysql中查找，找到之后存入redis
        Note byId = noteMapper.getById(id);
        if (byId != null) {
            redisUtil.setObject(RedisConstant.NOTE_DETAIL_KEY + id, byId, RedisConstant.NOTE_LIST_TTL, TimeUnit.SECONDS);
        }
        return byId;
    }
    @Override
    @Transactional
    public void updateNote(UpdateNoteDTO updateNoteDTO) {
        Note note = new Note();
        note.setId(updateNoteDTO.getId());
        note.setTitle(updateNoteDTO.getTitle());
        note.setContent(updateNoteDTO.getContent());
        note.setCategoryId(updateNoteDTO.getCategoryId());
        note.setUpdateTime(LocalDateTime.now());
        note.setIsFavorite(updateNoteDTO.getIsFavorite());
        noteMapper.updateById(note);
        // 先删除旧标签关联，再插入新的
        noteMapper.deleteNoteTags(updateNoteDTO.getId());
        if (!CollectionUtils.isEmpty(updateNoteDTO.getTagIds())) {
            noteMapper.insertNoteTags(updateNoteDTO.getId(), updateNoteDTO.getTagIds());
        }
        redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
        redisUtil.delete(RedisConstant.NOTE_DETAIL_KEY + updateNoteDTO.getId());
        // 写库成功后发 RabbitMQ 消息，由消费者异步重建全文索引。
        noteIndexMessageProducer.sendAfterCommit(updateNoteDTO.getId(), UserContext.getUserId(), "UPDATE");
    }

    @Override
    @Transactional
    public boolean deleteNote(Long id) {
        Note note = noteMapper.getById(id);
        if (note == null) {
            return false;
        }

        LocalDateTime deletedAt = LocalDateTime.now();
        Note update = new Note();
        update.setId(id);
        update.setIsDeleted(1);
        update.setDeletedTime(deletedAt);
        update.setUpdateTime(deletedAt);

        boolean movedToTrash = updateById(update);
        if (movedToTrash) {
            redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
            redisUtil.delete(RedisConstant.NOTE_DETAIL_KEY + id);
            // 回收站内的笔记不再参与搜索，因此先从 ES 删除文档。
            noteIndexMessageProducer.sendAfterCommit(id, note.getUserId(), "DELETE");
            // RabbitMQ TTL 队列 30 天后死信到 DLQ，DLQ 消费者再执行物理删除。
            noteDeleteDelayProducer.sendAfterCommit(id, note.getUserId(), deletedAt);
        }
        return movedToTrash;
    }

    @Override
    @Transactional
    public boolean restoreNote(Long id) {
        Note note = noteMapper.getById(id);
        if (note == null || !note.getUserId().equals(UserContext.getUserId())) {
            return false;
        }

        LambdaUpdateWrapper<Note> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Note::getId, id)
                .set(Note::getIsDeleted, 0)
                .set(Note::getDeletedTime, null)
                .set(Note::getUpdateTime, LocalDateTime.now());

        boolean restored = update(wrapper);
        if (restored) {
            redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
            redisUtil.delete(RedisConstant.NOTE_DETAIL_KEY + id);
            // 恢复后重新写入 ES，搜索结果重新可见。
            noteIndexMessageProducer.sendAfterCommit(id, note.getUserId(), "UPDATE");
        }
        return restored;
    }

    @Override
    @Transactional
    public boolean purgeExpiredDeletedNote(Long id) {
        Note note = noteMapper.getById(id);
        if (note == null || note.getIsDeleted() == null || note.getIsDeleted() == 0) {
            return false;
        }
        if (note.getDeletedTime() == null || note.getDeletedTime().isAfter(LocalDateTime.now().minusDays(30))) {
            return false;
        }

        noteMapper.deleteNoteTags(id);
        noteAttachmentMapper.deleteByNoteId(id);
        boolean removed = removeById(id);
        if (removed) {
            redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + note.getUserId() + ":*");
            redisUtil.delete(RedisConstant.NOTE_DETAIL_KEY + id);
            noteIndexMessageProducer.sendAfterCommit(id, note.getUserId(), "DELETE");
        }
        return removed;
    }

    @Override
    @Transactional
    public void pinNote(Long id) {
        Note note = noteMapper.getById(id);
        if (note == null) {
            return;
        }
        Note update = new Note();
        update.setId(id);
        update.setIsPinned(note.getIsPinned() == null || note.getIsPinned() == 0 ? 1 : 0);
        noteMapper.updateById(update);
        redisUtil.deleteByPattern(RedisConstant.NOTE_LIST_KEY + UserContext.getUserId() + ":*");
        redisUtil.delete(RedisConstant.NOTE_DETAIL_KEY + id);
        // 置顶状态也属于 ES 文档字段，需要同步到搜索索引。
        noteIndexMessageProducer.sendAfterCommit(id, note.getUserId(), "UPDATE");
    }
}
