package com.keepasd.knowledgebase.mq;

import com.keepasd.knowledgebase.config.RabbitMqConfig;
import com.keepasd.knowledgebase.service.FullTextIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoteIndexMessageConsumer {

    private final FullTextIndexService fullTextIndexService;

    public NoteIndexMessageConsumer(FullTextIndexService fullTextIndexService) {
        this.fullTextIndexService = fullTextIndexService;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTE_INDEX_QUEUE)
    public void consume(NoteIndexMessage message) {
        // 消费者只关心“同步某篇笔记索引”，具体索引介质交给 service 实现。
        log.info("收到笔记全文索引更新消息，noteId={}, userId={}, eventType={}",
                message.getNoteId(), message.getUserId(), message.getEventType());

        if ("DELETE".equals(message.getEventType())) {
            fullTextIndexService.deleteNoteIndex(message.getNoteId());
            return;
        }

        fullTextIndexService.rebuildNoteIndex(message.getNoteId());
    }
}
