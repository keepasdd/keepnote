package com.keepasd.knowledgebase.service.impl;

import com.keepasd.knowledgebase.document.NoteDocument;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.mapper.NoteMapper;
import com.keepasd.knowledgebase.repository.elasticsearch.NoteDocumentRepository;
import com.keepasd.knowledgebase.service.FullTextIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElasticsearchFullTextIndexServiceImpl implements FullTextIndexService {

    private final NoteMapper noteMapper;
    private final NoteDocumentRepository noteDocumentRepository;

    public ElasticsearchFullTextIndexServiceImpl(NoteMapper noteMapper,
                                                 NoteDocumentRepository noteDocumentRepository) {
        this.noteMapper = noteMapper;
        this.noteDocumentRepository = noteDocumentRepository;
    }

    @Override
    public void rebuildNoteIndex(Long noteId) {
        Note note = noteMapper.getById(noteId);
        if (note == null) {
            deleteNoteIndex(noteId);
            log.info("MySQL 中不存在该笔记，已删除 ES 索引文档，noteId={}", noteId);
            return;
        }

        noteDocumentRepository.save(toDocument(note));
        log.info("笔记已同步到 Elasticsearch，noteId={}, userId={}", note.getId(), note.getUserId());
    }

    @Override
    public void deleteNoteIndex(Long noteId) {
        // 删除操作天然幂等：ES 中没有该文档时也不会影响主流程。
        noteDocumentRepository.deleteById(noteId);
        log.info("笔记已从 Elasticsearch 删除，noteId={}", noteId);
    }

    private NoteDocument toDocument(Note note) {
        // ES 文档只保存搜索和筛选需要的字段，MySQL 仍然是业务主库。
        return new NoteDocument(
                note.getId(),
                note.getUserId(),
                note.getCategoryId(),
                safeText(note.getTitle()),
                safeText(note.getContent()),
                note.getIsFavorite(),
                note.getIsPinned(),
                note.getCreateTime(),
                note.getUpdateTime()
        );
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
