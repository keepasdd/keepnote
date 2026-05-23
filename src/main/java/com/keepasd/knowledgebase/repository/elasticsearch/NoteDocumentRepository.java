package com.keepasd.knowledgebase.repository.elasticsearch;

import com.keepasd.knowledgebase.document.NoteDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NoteDocumentRepository extends ElasticsearchRepository<NoteDocument, Long> {
}
