package io.github.adrian.wieczorek.local_trade.service.chat.service;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatSummaryDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.UnreadCountDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

public interface ChatMessageService {
    @Transactional
    ChatMessageEntity save(ChatMessageEntity chatMessageEntity);

    @Transactional
    ChatMessageDto createAndSaveMessageForPrivateUser(ChatMessagePayload chatMessage, Principal principal, String recipientEmail);

    @Transactional(readOnly = true)
    List<ChatMessageDto> getChatHistory(UserDetails sender, String recipientUsername);

    @Transactional
    void markMessagesAsRead(String senderEmail, String recipientEmail);

    @Transactional(readOnly = true)
    List<ChatSummaryDto> getInbox(String userEmail);
    @Transactional(readOnly = true)
    UnreadCountDto getTotalUnreadCount(String userEmail);
}
