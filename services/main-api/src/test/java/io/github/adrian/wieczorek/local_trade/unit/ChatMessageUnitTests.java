package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.mappers.ChatSummaryDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.chat.service.ChatMessageServiceImpl;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageUnitTests {
    @Mock
    ChatMessageRepository chatMessageRepository;
    @InjectMocks
    ChatMessageServiceImpl chatMessageService;
    @Mock
    UsersService usersService;
    @Mock
    ChatSummaryDtoMapper chatSummaryDtoMapper;

    @Test
    public void whenSearchingForRecipientAndSenderAndSendingMessage_thenReturnChatMessage() {
        UsersEntity user1 = UserUtils.createUserRoleUser();
        UsersEntity user2 = UserUtils.createUserRoleUser();
        user2.setEmail("Test2@test2.pl");

        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(user1.getEmail());

        ChatMessagePayload payload = new ChatMessagePayload("Test message");

        when(usersService.getCurrentUser(principal.getName())).thenReturn(user1);
        when(usersService.getCurrentUser(user2.getEmail())).thenReturn(user2);
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessageDto chatMessage = chatMessageService.createAndSaveMessageForPrivateUser(payload,principal,user2.getEmail());

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(chatMessageRepository).save(captor.capture());
        ChatMessageEntity saved = captor.getValue();

        Assertions.assertNotNull(saved);
        Assertions.assertEquals(payload.content(),saved.getContent());
        Assertions.assertEquals(user1,saved.getSender());
        Assertions.assertEquals(user2,saved.getRecipient());
        Assertions.assertNotNull(chatMessage);
        Assertions.assertEquals(payload.content(),chatMessage.getContent());
    }

    @Test
    public void whenSearchingForRecipientAndSenderAndSendingMessageSenderNotFound_thenReturnUserNotFound() {
        UsersEntity user1 = UserUtils.createUserRoleUser();
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(user1.getEmail());
        ChatMessagePayload payload = new ChatMessagePayload("Test message");

        when(usersService.getCurrentUser(principal.getName())).thenThrow(UserNotFoundException.class);

        Assertions.assertThrows(UserNotFoundException.class, () -> chatMessageService.createAndSaveMessageForPrivateUser(payload,principal,user1.getEmail()));
        verify(chatMessageRepository, never()).save(any(ChatMessageEntity.class));

    }

    @Test
    public void whenSearchingForRecipientAndSenderAndSendingMessageRecipientNotFound_thenReturnUserNotFound() {
        UsersEntity user1 = UserUtils.createUserRoleUser();
        UsersEntity user2 = UserUtils.createUserRoleUser();
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn(user1.getUsername());
        ChatMessagePayload payload = new ChatMessagePayload("Test message");
        when(usersService.getCurrentUser(principal.getName())).thenReturn(user1);
        when(usersService.getCurrentUser(user2.getEmail())).thenThrow(UserNotFoundException.class);

        Assertions.assertThrows(UserNotFoundException.class, () -> chatMessageService.createAndSaveMessageForPrivateUser(payload,principal,user2.getEmail()));
        verify(chatMessageRepository, never()).save(any(ChatMessageEntity.class));
    }

    @Test
    public void whenPullingMessageHistory_thenReturnChatMessageHistory() {
        UsersEntity user1 = UserUtils.createUserRoleUser();
        user1.setName("Adrian");

        UsersEntity user2 = UserUtils.createUserRoleUser();
        user2.setName("Kupiec");

        ChatMessageEntity messageEntity1 = ChatMessageEntity.builder()
                .sender(user2)
                .recipient(user1)
                .content("Hey its me!")
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build();

        ChatMessageEntity messageEntity2 = ChatMessageEntity.builder()
                .sender(user1)
                .recipient(user2)
                .content("Do you want my car?")
                .timestamp(LocalDateTime.now().minusMinutes(4))
                .build();

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(user1.getUsername());
        when(usersService.getCurrentUser(user1.getUsername())).thenReturn(user1);
        when(usersService.getCurrentUser(user2.getName())).thenReturn(user2);

        when(chatMessageRepository.findBySenderAndRecipient(user1, user2)).thenReturn(List.of(messageEntity2));
        when(chatMessageRepository.findBySenderAndRecipient(user2, user1)).thenReturn(List.of(messageEntity1));

        List<ChatMessageDto> result = chatMessageService.getChatHistory(userDetails, user2.getName());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals("Hey its me!", result.get(0).getContent());
        Assertions.assertEquals("Kupiec", result.get(0).getSender());

        Assertions.assertEquals("Do you want my car?", result.get(1).getContent());
        Assertions.assertEquals("Adrian", result.get(1).getSender());
    }

    @Test
    public void whenSearchingForRecipient_andRecipientNotFound_thenThrowException() {
        UsersEntity user1 = UserUtils.createUserRoleUser();
        UsersEntity user2 = UserUtils.createUserRoleUser();
        user2.setName("user2");

        UserDetails userDetails = Mockito.mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(user1.getUsername());
        when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(user1);

        when(usersService.getCurrentUser(user2.getName())).thenThrow(UserNotFoundException.class);

        Assertions.assertThrows(UserNotFoundException.class, () -> chatMessageService.getChatHistory(userDetails,user2.getName()));
    }
    @Test
    public void shouldInvokeRepositoryToMarkMessagesAsRead() {
            String senderEmail = "sender@test.pl";
            String recipientEmail = "recipient@test.pl";

            UsersEntity sender = new UsersEntity();
            sender.setEmail(senderEmail);

            UsersEntity recipient = new UsersEntity();
            recipient.setEmail(recipientEmail);

            when(usersService.getCurrentUser(senderEmail)).thenReturn(sender);
            when(usersService.getCurrentUser(recipientEmail)).thenReturn(recipient);

            chatMessageService.markMessagesAsRead(senderEmail, recipientEmail);

            verify(usersService, times(1)).getCurrentUser(senderEmail);
            verify(usersService, times(1)).getCurrentUser(recipientEmail);

            verify(chatMessageRepository, times(1)).markAllAsRead(sender, recipient);
        }
    @Test
    @DisplayName("Should correctly identify partner and map to Inbox DTO")
    void shouldReturnCorrectInbox() {

        UsersEntity me = UserUtils.createUserRoleUser();
        UsersEntity partner = UserUtils.createUserRoleUser();

        ChatMessageEntity lastMsg = ChatMessageEntity.builder()
                .sender(me).recipient(partner).content("Hej!").build();


        when(usersService.getCurrentUser(me.getEmail())).thenReturn(me);
        when(chatMessageRepository.findLastMessagesPerConversation(me)).thenReturn(List.of(lastMsg));
        when(chatMessageRepository.countUnreadFromPartner(partner, me)).thenReturn(2L);

        chatMessageService.getInbox(me.getEmail());

        verify(chatSummaryDtoMapper).toChatSummaryDto(lastMsg, partner, 2L);
    }
}
