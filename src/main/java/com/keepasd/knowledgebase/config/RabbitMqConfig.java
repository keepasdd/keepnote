package com.keepasd.knowledgebase.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTE_INDEX_EXCHANGE = "keepnote.note.index.exchange";
    public static final String NOTE_INDEX_QUEUE = "keepnote.note.index.queue";
    public static final String NOTE_INDEX_ROUTING_KEY = "note.index.update";
    public static final String NOTE_DELETE_DELAY_EXCHANGE = "keepnote.note.delete.delay.exchange";
    public static final String NOTE_DELETE_DELAY_QUEUE = "keepnote.note.delete.delay.queue";
    public static final String NOTE_DELETE_DELAY_ROUTING_KEY = "note.delete.delay";
    public static final String NOTE_DELETE_DLX = "keepnote.note.delete.dlx";
    public static final String NOTE_DELETE_DLQ = "keepnote.note.delete.dlq";
    public static final String NOTE_DELETE_DLQ_ROUTING_KEY = "note.delete.expired";
    public static final long NOTE_DELETE_TTL = 30L * 24 * 60 * 60 * 1000;

    @Bean
    public DirectExchange noteIndexExchange() {
        // 持久化交换机：服务重启后 RabbitMQ 仍保留交换机定义。
        return new DirectExchange(NOTE_INDEX_EXCHANGE, true, false);
    }

    @Bean
    public Queue noteIndexQueue() {
        // 持久化队列：索引消息不会因为 RabbitMQ 重启直接丢失。
        return new Queue(NOTE_INDEX_QUEUE, true);
    }

    @Bean
    public Binding noteIndexBinding(DirectExchange noteIndexExchange, Queue noteIndexQueue) {
        // 绑定路由键，生产者发到 exchange 后会进入全文索引队列。
        return BindingBuilder.bind(noteIndexQueue)
                .to(noteIndexExchange)
                .with(NOTE_INDEX_ROUTING_KEY);
    }

    @Bean
    public DirectExchange noteDeleteDelayExchange() {
        return new DirectExchange(NOTE_DELETE_DELAY_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange noteDeleteDlx() {
        return new DirectExchange(NOTE_DELETE_DLX, true, false);
    }

    @Bean
    public Queue noteDeleteDelayQueue() {
        // 删除消息先进入 TTL 队列，30 天未被消费后变成死信，再进入真正执行物理删除的 DLQ。
        return QueueBuilder.durable(NOTE_DELETE_DELAY_QUEUE)
                .withArgument("x-message-ttl", NOTE_DELETE_TTL)
                .withArgument("x-dead-letter-exchange", NOTE_DELETE_DLX)
                .withArgument("x-dead-letter-routing-key", NOTE_DELETE_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue noteDeleteDlq() {
        // DLQ 消费者只处理已过 30 天且仍在回收站的笔记。
        return QueueBuilder.durable(NOTE_DELETE_DLQ).build();
    }

    @Bean
    public Binding noteDeleteDelayBinding(DirectExchange noteDeleteDelayExchange, Queue noteDeleteDelayQueue) {
        return BindingBuilder.bind(noteDeleteDelayQueue)
                .to(noteDeleteDelayExchange)
                .with(NOTE_DELETE_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding noteDeleteDlqBinding(DirectExchange noteDeleteDlx, Queue noteDeleteDlq) {
        return BindingBuilder.bind(noteDeleteDlq)
                .to(noteDeleteDlx)
                .with(NOTE_DELETE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        // 使用 JSON 传输 DTO，避免 Java 原生序列化带来的兼容性问题。
        return new Jackson2JsonMessageConverter();
    }
}
