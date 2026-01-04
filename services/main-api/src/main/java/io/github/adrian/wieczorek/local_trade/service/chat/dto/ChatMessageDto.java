package io.github.adrian.wieczorek.local_trade.service.chat.dto;

import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime timestamp;
    private boolean isRead;

    public ChatMessageDto(ChatMessageEntity entity) {
        this.content = entity.getContent();
        this.sender = entity.getSender().getName();
        this.recipient = entity.getRecipient().getName();
        this.timestamp = entity.getTimestamp();
        this.isRead = entity.isRead();
    }
}

