package io.github.adrian.wieczorek.local_trade.service.chat.service;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatSummaryDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.UnreadCountDto;
import io.github.adrian.wieczorek.local_trade.service.chat.mappers.ChatSummaryDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    public ChatMessageDto createAndSaveMessageForPrivateUser(ChatMessagePayload chatMessage, Principal principal, String recipientEmail) {
        UsersEntity user = usersService.getCurrentUser(principal.getName());
        UsersEntity user1 = usersService.getCurrentUser(recipientEmail);
        ChatMessageEntity newChatMessageEntity = ChatMessageEntity.builder()
                .sender(user)
                .recipient(user1)
                .content(chatMessage.content())
                .build();
        ChatMessageEntity savedEntity = chatMessageRepository.save(newChatMessageEntity);
        return new ChatMessageDto(savedEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDto> getChatHistory(UserDetails sender, String recipientUsername) {
        UsersEntity user1 = usersService.getCurrentUser(sender.getUsername());
        UsersEntity user2 = usersService.getCurrentUser(recipientUsername);

        List<ChatMessageEntity> history1 = chatMessageRepository.findBySenderAndRecipient(user1, user2);
        List<ChatMessageEntity> history2 = chatMessageRepository.findBySenderAndRecipient(user2, user1);

        List<ChatMessageEntity> fullHistory = new ArrayList<>();
        fullHistory.addAll(history1);
        fullHistory.addAll(history2);

        fullHistory.sort(Comparator.comparing(ChatMessageEntity::getTimestamp));

        return fullHistory.stream()
                .map(ChatMessageDto::new)
                .toList();
    }

    @Override
    public void markMessagesAsRead(String senderEmail, String recipientEmail) {
        UsersEntity sender = usersService.getCurrentUser(senderEmail);
        UsersEntity recipient = usersService.getCurrentUser(recipientEmail);

        chatMessageRepository.markAllAsRead(sender,recipient);
    }

        @Transactional(readOnly = true)
        @Override
        public List<ChatSummaryDto> getInbox(String userEmail) {
            UsersEntity currentUser = usersService.getCurrentUser(userEmail);

            List<ChatMessageEntity> lastMessages = chatMessageRepository.findLastMessagesPerConversation(currentUser);

            return lastMessages.stream().map(msg -> {
                UsersEntity partner = msg.getSender().equals(currentUser) ? msg.getRecipient() : msg.getSender();

                long unreadCount = chatMessageRepository.countUnreadFromPartner(partner, currentUser);

                return chatSummaryDtoMapper.toChatSummaryDto(msg, partner, unreadCount);
            }).toList();
        }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountDto getTotalUnreadCount(String userEmail) {
        UsersEntity currentUser = usersService.getCurrentUser(userEmail);

        long count = chatMessageRepository.countTotalUnread(currentUser);

        return chatSummaryDtoMapper.toUnreadCountDto(count);
    }
}