package com.keepasd.knowledgebase.mq;

import com.keepasd.knowledgebase.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Component
public class NoteIndexMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public NoteIndexMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAfterCommit(Long noteId, Long userId, String eventType) {
        NoteIndexMessage message = new NoteIndexMessage(noteId, userId, eventType, LocalDateTime.now());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 等数据库事务提交成功后再发消息，避免消费者读到未提交或回滚的数据。
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(message);
                }
            });
            return;
        }

        send(message);
    }

    private void send(NoteIndexMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTE_INDEX_EXCHANGE,
                    RabbitMqConfig.NOTE_INDEX_ROUTING_KEY,
                    message
            );
            log.info("已发送笔记全文索引更新消息，noteId={}, userId={}, eventType={}",
                    message.getNoteId(), message.getUserId(), message.getEventType());
        } catch (AmqpException e) {
            // 索引是异步增强能力，消息发送失败时记录日志，避免影响笔记主流程。
            log.error("发送笔记全文索引更新消息失败，noteId={}", message.getNoteId(), e);
        }
    }
}
