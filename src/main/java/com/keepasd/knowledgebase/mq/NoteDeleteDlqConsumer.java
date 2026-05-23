package com.keepasd.knowledgebase.mq;

import com.keepasd.knowledgebase.config.RabbitMqConfig;
import com.keepasd.knowledgebase.service.NoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoteDeleteDlqConsumer {

    private final NoteService noteService;

    public NoteDeleteDlqConsumer(NoteService noteService) {
        this.noteService = noteService;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTE_DELETE_DLQ)
    public void consume(NoteDeleteDelayMessage message) {
        // 延迟消息进入 DLQ 后才尝试物理删除；如果用户已恢复，service 会直接跳过。
        boolean removed = noteService.purgeExpiredDeletedNote(message.getNoteId());
        log.info("处理回收站笔记物理删除 DLQ 消息，noteId={}, removed={}", message.getNoteId(), removed);
    }
}
