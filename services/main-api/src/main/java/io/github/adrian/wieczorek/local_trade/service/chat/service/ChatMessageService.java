package io.github.adrian.wieczorek.local_trade.service.chat.service;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

public interface ChatMessageService {
  @Transactional
  ChatMessageEntity save(ChatMessageEntity chatMessageEntity);

  @Transactional
  ChatMessageDto createAndSaveMessageForPrivateUser(ChatMessagePayload chatMessage,
      Principal principal, String recipientEmail);

  @Transactional
  void markMessagesAsRead(String senderEmail, String recipientEmail);

}
