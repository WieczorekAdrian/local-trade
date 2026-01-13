package io.github.adrian.wieczorek.local_trade.service.chat.service;

import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatSummaryDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.PartnerUnreadCountDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.UnreadCountDto;
import io.github.adrian.wieczorek.local_trade.service.chat.mappers.ChatSummaryDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ChatMessageFinder {

  private final ChatMessageRepository chatMessageRepository;
  private final UsersService usersService;
  private final ChatSummaryDtoMapper chatSummaryDtoMapper;

  public List<ChatMessageDto> getChatHistory(UserDetails sender, String recipientUsername) {
    UsersEntity user1 = usersService.getCurrentUser(sender.getUsername());
    UsersEntity user2 = usersService.getCurrentUser(recipientUsername);

    List<ChatMessageEntity> fullHistory =
        chatMessageRepository.findBySenderAndRecipient(user1, user2);

    return fullHistory.stream().map(ChatMessageDto::new).toList();
  }

  public List<ChatSummaryDto> getInbox(String userEmail) {
    UsersEntity currentUser = usersService.getCurrentUser(userEmail);

    List<ChatMessageEntity> lastMessages =
        chatMessageRepository.findLastMessagesPerConversation(currentUser);

    return lastMessages.stream().map(msg -> {
      UsersEntity partner =
          msg.getSender().equals(currentUser) ? msg.getRecipient() : msg.getSender();

      long unreadCount = chatMessageRepository.countUnreadFromPartner(partner, currentUser);

      return chatSummaryDtoMapper.toChatSummaryDto(msg, partner, unreadCount);
    }).toList();
  }

  public List<ChatSummaryDto> getMessagesForMessageBox(String userEmail) {
    UsersEntity currentUser = usersService.getCurrentUser(userEmail);

    List<ChatMessageEntity> lastMessages =
        chatMessageRepository.findLastMessagesPerConversation(currentUser);

    if (lastMessages.isEmpty()) {
      return Collections.emptyList();
    }

    List<UsersEntity> partners = lastMessages.stream()
        .map(msg -> msg.getSender().equals(currentUser) ? msg.getRecipient() : msg.getSender())
        .distinct().toList();

    List<PartnerUnreadCountDto> countRaw =
        chatMessageRepository.countUnreadForPartners(partners, currentUser);

    Map<UsersEntity, Long> unreadCountsMap = countRaw.stream()
        .collect(Collectors.toMap(PartnerUnreadCountDto::partner, PartnerUnreadCountDto::count));
    return lastMessages.stream().map(msg -> {
      UsersEntity partner =
          msg.getSender().equals(currentUser) ? msg.getRecipient() : msg.getSender();
      long unreadCount = unreadCountsMap.getOrDefault(partner, 0L);
      return chatSummaryDtoMapper.toChatSummaryDto(msg, partner, unreadCount);
    }).toList();
  }

  public UnreadCountDto getTotalUnreadCount(String userEmail) {
    UsersEntity currentUser = usersService.getCurrentUser(userEmail);

    long count = chatMessageRepository.countTotalUnread(currentUser);

    return chatSummaryDtoMapper.toUnreadCountDto(count);
  }
}
