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
public class NoteDeleteDelayProducer {

    private final RabbitTemplate rabbitTemplate;

    public NoteDeleteDelayProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAfterCommit(Long noteId, Long userId, LocalDateTime deletedAt) {
        NoteDeleteDelayMessage message = new NoteDeleteDelayMessage(noteId, userId, deletedAt);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 只有 MySQL 成功标记回收站后，才发送 30 天延迟删除消息。
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

    private void send(NoteDeleteDelayMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTE_DELETE_DELAY_EXCHANGE,
                    RabbitMqConfig.NOTE_DELETE_DELAY_ROUTING_KEY,
                    message
            );
            log.info("已发送笔记延迟物理删除消息，noteId={}, userId={}", message.getNoteId(), message.getUserId());
        } catch (AmqpException e) {
            log.error("发送笔记延迟物理删除消息失败，noteId={}", message.getNoteId(), e);
        }
    }
}
