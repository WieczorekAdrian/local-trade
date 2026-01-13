package io.github.adrian.wieczorek.local_trade.service.chat.service;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.mappers.ChatSummaryDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final UsersService usersService;
  private final ChatSummaryDtoMapper chatSummaryDtoMapper;

  @Override
  @Transactional
  public ChatMessageEntity save(ChatMessageEntity chatMessageEntity) {
    chatMessageEntity.setTimestamp(LocalDateTime.now());
    return chatMessageRepository.save(chatMessageEntity);
  }

  @Transactional
  @Override
  public ChatMessageDto createAndSaveMessageForPrivateUser(ChatMessagePayload chatMessage,
      Principal principal, String recipientEmail) {
    UsersEntity user = usersService.getCurrentUser(principal.getName());
    UsersEntity user1 = usersService.getCurrentUser(recipientEmail);
    ChatMessageEntity newChatMessageEntity = ChatMessageEntity.builder().sender(user)
        .recipient(user1).content(chatMessage.content()).build();
    ChatMessageEntity savedEntity = chatMessageRepository.save(newChatMessageEntity);
    return new ChatMessageDto(savedEntity);
  }



  @Override
  public void markMessagesAsRead(String senderEmail, String recipientEmail) {
    UsersEntity sender = usersService.getCurrentUser(senderEmail);
    UsersEntity recipient = usersService.getCurrentUser(recipientEmail);

    chatMessageRepository.markAllAsRead(sender, recipient);
  }

}
