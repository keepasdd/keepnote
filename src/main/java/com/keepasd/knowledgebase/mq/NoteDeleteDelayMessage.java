package com.keepasd.knowledgebase.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDeleteDelayMessage {

    private Long noteId;
    private Long userId;
    private LocalDateTime deletedAt;
}
