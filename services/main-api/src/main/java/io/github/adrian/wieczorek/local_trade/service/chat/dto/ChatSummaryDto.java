package io.github.adrian.wieczorek.local_trade.service.chat.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatSummaryDto(
        String lastMessage,
        String partnerName,
        String partnerEmail,
        LocalDateTime lastMessageTimestamp,
        long unreadCount
) {}
