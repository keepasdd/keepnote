package com.keepasd.knowledgebase.sync;

import com.keepasd.knowledgebase.document.NoteDocument;
import com.keepasd.knowledgebase.entity.Note;
import com.keepasd.knowledgebase.mapper.NoteMapper;
import com.keepasd.knowledgebase.repository.elasticsearch.NoteDocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NoteElasticsearchStartupSyncRunner implements ApplicationRunner {

    private final NoteMapper noteMapper;
    private final NoteDocumentRepository noteDocumentRepository;

    public NoteElasticsearchStartupSyncRunner(NoteMapper noteMapper,
                                              NoteDocumentRepository noteDocumentRepository) {
        this.noteMapper = noteMapper;
        this.noteDocumentRepository = noteDocumentRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Note> notes = noteMapper.selectList(null);
            List<NoteDocument> documents = notes.stream()
                    .map(this::toDocument)
                    .toList();
            noteDocumentRepository.saveAll(documents);
            log.info("启动时 MySQL -> Elasticsearch 全量同步完成，count={}", documents.size());
        } catch (Exception e) {
            // ES 可能比应用启动得慢；增量同步仍会通过 RabbitMQ 重试/补偿，启动不因此失败。
            log.warn("启动时 MySQL -> Elasticsearch 全量同步失败，请确认 ES 已启动且 note 表字段已更新，reason={}", e.getMessage());
        }
    }

    private NoteDocument toDocument(Note note) {
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
