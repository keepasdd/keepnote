package com.keepasd.knowledgebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.kotlin.KtQueryChainWrapper;
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateChainWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.keepasd.knowledgebase.common.PageResult;
import com.keepasd.knowledgebase.dto.request.NoteQueryDTO;
import com.keepasd.knowledgebase.dto.request.UpdateNoteDTO;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.mapper.NoteMapper;
import com.keepasd.knowledgebase.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Override
    public boolean addNote(Note note) {
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());
        return noteMapper.insert(note) > 0;
    }

    @Override
    public PageResult pageQuery(NoteQueryDTO noteQueryDTO) {
        PageHelper.startPage(noteQueryDTO.getPage(), noteQueryDTO.getPageSize());
        Page<Note> page = noteMapper.pageQuery(noteQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Note getbyId(Long id) {
        Note note = noteMapper.getById(id);
        return note;
    }

    @Override
    public void updateNote(UpdateNoteDTO updateNoteDTO) {
        Note note = new Note();
        note.setId(updateNoteDTO.getId());
        note.setTitle(updateNoteDTO.getTitle());
        note.setContent(updateNoteDTO.getContent());
        note.setCategoryId(updateNoteDTO.getCategoryId());
        note.setUpdateTime(LocalDateTime.now());
        noteMapper.updateById(note);
    }

    @Override
    public boolean save(Note entity) {
        return NoteService.super.save(entity);
    }

    @Override
    public boolean saveBatch(Collection<Note> entityList) {
        return NoteService.super.saveBatch(entityList);
    }

    @Override
    public boolean saveBatch(Collection<Note> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Note> entityList) {
        return NoteService.super.saveOrUpdateBatch(entityList);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<Note> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean removeById(Serializable id) {
        return NoteService.super.removeById(id);
    }

    @Override
    public boolean removeById(Serializable id, boolean useFill) {
        return NoteService.super.removeById(id, useFill);
    }

    @Override
    public boolean removeById(Note entity) {
        return NoteService.super.removeById(entity);
    }

    @Override
    public boolean removeByMap(Map<String, Object> columnMap) {
        return NoteService.super.removeByMap(columnMap);
    }

    @Override
    public boolean remove(Wrapper<Note> queryWrapper) {
        return NoteService.super.remove(queryWrapper);
    }

    @Override
    public boolean removeByIds(Collection<?> list) {
        return NoteService.super.removeByIds(list);
    }

    @Override
    public boolean removeByIds(Collection<?> list, boolean useFill) {
        return NoteService.super.removeByIds(list, useFill);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list) {
        return NoteService.super.removeBatchByIds(list);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list, boolean useFill) {
        return NoteService.super.removeBatchByIds(list, useFill);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list, int batchSize) {
        return NoteService.super.removeBatchByIds(list, batchSize);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list, int batchSize, boolean useFill) {
        return NoteService.super.removeBatchByIds(list, batchSize, useFill);
    }

    @Override
    public boolean updateById(Note entity) {
        return NoteService.super.updateById(entity);
    }

    @Override
    public boolean update(Wrapper<Note> updateWrapper) {
        return NoteService.super.update(updateWrapper);
    }

    @Override
    public boolean update(Note entity, Wrapper<Note> updateWrapper) {
        return NoteService.super.update(entity, updateWrapper);
    }

    @Override
    public boolean updateBatchById(Collection<Note> entityList) {
        return NoteService.super.updateBatchById(entityList);
    }

    @Override
    public boolean updateBatchById(Collection<Note> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(Note entity) {
        return false;
    }

    @Override
    public Note getById(Serializable id) {
        return NoteService.super.getById(id);
    }

    @Override
    public Optional<Note> getOptById(Serializable id) {
        return NoteService.super.getOptById(id);
    }

    @Override
    public List<Note> listByIds(Collection<? extends Serializable> idList) {
        return NoteService.super.listByIds(idList);
    }

    @Override
    public List<Note> listByMap(Map<String, Object> columnMap) {
        return NoteService.super.listByMap(columnMap);
    }

    @Override
    public Note getOne(Wrapper<Note> queryWrapper) {
        return NoteService.super.getOne(queryWrapper);
    }

    @Override
    public Optional<Note> getOneOpt(Wrapper<Note> queryWrapper) {
        return NoteService.super.getOneOpt(queryWrapper);
    }

    @Override
    public Note getOne(Wrapper<Note> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<Note> getOneOpt(Wrapper<Note> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<Note> queryWrapper) {
        return Map.of();
    }

    @Override
    public <V> V getObj(Wrapper<Note> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public boolean exists(Wrapper<Note> queryWrapper) {
        return NoteService.super.exists(queryWrapper);
    }

    @Override
    public long count() {
        return NoteService.super.count();
    }

    @Override
    public long count(Wrapper<Note> queryWrapper) {
        return NoteService.super.count(queryWrapper);
    }

    @Override
    public List<Note> list(Wrapper<Note> queryWrapper) {
        return NoteService.super.list(queryWrapper);
    }

    @Override
    public List<Note> list(IPage<Note> page, Wrapper<Note> queryWrapper) {
        return NoteService.super.list(page, queryWrapper);
    }

    @Override
    public List<Note> list() {
        return NoteService.super.list();
    }

    @Override
    public List<Note> list(IPage<Note> page) {
        return NoteService.super.list(page);
    }

    @Override
    public <E extends IPage<Note>> E page(E page, Wrapper<Note> queryWrapper) {
        return NoteService.super.page(page, queryWrapper);
    }

    @Override
    public <E extends IPage<Note>> E page(E page) {
        return NoteService.super.page(page);
    }

    @Override
    public List<Map<String, Object>> listMaps(Wrapper<Note> queryWrapper) {
        return NoteService.super.listMaps(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> listMaps(IPage<? extends Map<String, Object>> page, Wrapper<Note> queryWrapper) {
        return NoteService.super.listMaps(page, queryWrapper);
    }

    @Override
    public List<Map<String, Object>> listMaps() {
        return NoteService.super.listMaps();
    }

    @Override
    public List<Map<String, Object>> listMaps(IPage<? extends Map<String, Object>> page) {
        return NoteService.super.listMaps(page);
    }

    @Override
    public <E> List<E> listObjs() {
        return NoteService.super.listObjs();
    }

    @Override
    public <V> List<V> listObjs(Function<? super Object, V> mapper) {
        return NoteService.super.listObjs(mapper);
    }

    @Override
    public <E> List<E> listObjs(Wrapper<Note> queryWrapper) {
        return NoteService.super.listObjs(queryWrapper);
    }

    @Override
    public <V> List<V> listObjs(Wrapper<Note> queryWrapper, Function<? super Object, V> mapper) {
        return NoteService.super.listObjs(queryWrapper, mapper);
    }

    @Override
    public <E extends IPage<Map<String, Object>>> E pageMaps(E page, Wrapper<Note> queryWrapper) {
        return NoteService.super.pageMaps(page, queryWrapper);
    }

    @Override
    public <E extends IPage<Map<String, Object>>> E pageMaps(E page) {
        return NoteService.super.pageMaps(page);
    }

    @Override
    public BaseMapper<Note> getBaseMapper() {
        return null;
    }

    @Override
    public Class<Note> getEntityClass() {
        return null;
    }

    @Override
    public QueryChainWrapper<Note> query() {
        return NoteService.super.query();
    }

    @Override
    public LambdaQueryChainWrapper<Note> lambdaQuery() {
        return NoteService.super.lambdaQuery();
    }

    @Override
    public LambdaQueryChainWrapper<Note> lambdaQuery(Note entity) {
        return NoteService.super.lambdaQuery(entity);
    }

    @Override
    public KtQueryChainWrapper<Note> ktQuery() {
        return NoteService.super.ktQuery();
    }

    @Override
    public KtUpdateChainWrapper<Note> ktUpdate() {
        return NoteService.super.ktUpdate();
    }

    @Override
    public UpdateChainWrapper<Note> update() {
        return NoteService.super.update();
    }

    @Override
    public LambdaUpdateChainWrapper<Note> lambdaUpdate() {
        return NoteService.super.lambdaUpdate();
    }

    @Override
    public boolean saveOrUpdate(Note entity, Wrapper<Note> updateWrapper) {
        return NoteService.super.saveOrUpdate(entity, updateWrapper);
    }
}
