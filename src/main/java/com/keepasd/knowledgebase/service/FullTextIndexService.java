package com.keepasd.knowledgebase.service;

public interface FullTextIndexService {

    void rebuildNoteIndex(Long noteId);

    void deleteNoteIndex(Long noteId);
}
