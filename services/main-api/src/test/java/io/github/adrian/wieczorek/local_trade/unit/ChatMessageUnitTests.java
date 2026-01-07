package io.github.adrian.wieczorek.local_trade.unit;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
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
        user1.setEmail("adrian@test.pl");

        UsersEntity user2 = UserUtils.createUserRoleUser();
        user2.setName("Kupiec");
        user2.setEmail("kupiec@test.pl");

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

        List<ChatMessageEntity> conversationFromDb = List.of(messageEntity1, messageEntity2);

        when(chatMessageRepository.findBySenderAndRecipient(user1, user2))
                .thenReturn(conversationFromDb);

        List<ChatMessageDto> result = chatMessageService.getChatHistory(userDetails, user2.getName());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());

        Assertions.assertEquals("Hey its me!", result.get(0).getContent());
        Assertions.assertEquals(user2.getEmail(), result.get(0).getSender());

        Assertions.assertEquals("Do you want my car?", result.get(1).getContent());
        Assertions.assertEquals(user1.getEmail(), result.get(1).getSender());
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
        @DisplayName("Should correctly identify partner when partner is the sender")
    public void getInbox_HappyPath_PartnerIsSender() {
        UsersEntity me = UserUtils.createUserRoleUser();
        UsersEntity partner = UserUtils.createUserRoleUser();
        partner.setEmail("partner@test.pl");

        ChatMessageEntity lastMsg = ChatMessageEntity.builder()
                .sender(partner).recipient(me).content("Cześć Adrian!").build();

        when(usersService.getCurrentUser(me.getEmail())).thenReturn(me);
        when(chatMessageRepository.findLastMessagesPerConversation(me)).thenReturn(List.of(lastMsg));
        when(chatMessageRepository.countUnreadFromPartner(partner, me)).thenReturn(1L);

        chatMessageService.getInbox(me.getEmail());

        verify(chatSummaryDtoMapper).toChatSummaryDto(lastMsg, partner, 1L);
    }

    @Test
    @DisplayName("Should return empty list when user has no conversations")
    public void getInbox_HappyPath_EmptyInbox() {
        UsersEntity me = UserUtils.createUserRoleUser();
        when(usersService.getCurrentUser(me.getEmail())).thenReturn(me);
        when(chatMessageRepository.findLastMessagesPerConversation(me)).thenReturn(List.of());

        List<ChatSummaryDto> result = chatMessageService.getInbox(me.getEmail());

        assertThat(result).isEmpty();
        verify(chatMessageRepository, never()).countUnreadFromPartner(any(), any());
        verify(chatSummaryDtoMapper, never()).toChatSummaryDto(any(), any(), anyLong());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    public void getInbox_BadPath_UserNotFound() {
        String fakeEmail = "fake@test.pl";
        when(usersService.getCurrentUser(fakeEmail)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> chatMessageService.getInbox(fakeEmail));
        verify(chatMessageRepository, never()).findLastMessagesPerConversation(any());
    }

    @Test
    @DisplayName("Should throw exception when repository fails")
    public void getInbox_BadPath_RepositoryError() {
        UsersEntity me = UserUtils.createUserRoleUser();
        when(usersService.getCurrentUser(me.getEmail())).thenReturn(me);
        when(chatMessageRepository.findLastMessagesPerConversation(me)).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> chatMessageService.getInbox(me.getEmail()));
    }
    @Test
    @DisplayName("markMessagesAsRead: Bad Path - Sender does not exist")
    void markMessagesAsRead_SenderNotFound() {
        String senderEmail = "nonexistent@test.pl";
        String recipientEmail = "recipient@test.pl";

        when(usersService.getCurrentUser(senderEmail))
                .thenThrow(new UserNotFoundException("Sender not found"));

        assertThrows(UserNotFoundException.class, () ->
                chatMessageService.markMessagesAsRead(senderEmail, recipientEmail)
        );

        verify(chatMessageRepository, never()).markAllAsRead(any(), any());

        verify(usersService, never()).getCurrentUser(recipientEmail);
    }

    @Test
    @DisplayName("markMessagesAsRead: Bad Path - Recipient does not exist")
    void markMessagesAsRead_RecipientNotFound() {
        String senderEmail = "sender@test.pl";
        String recipientEmail = "nonexistent@test.pl";

        UsersEntity sender = new UsersEntity();
        sender.setEmail(senderEmail);

        when(usersService.getCurrentUser(senderEmail)).thenReturn(sender);
        when(usersService.getCurrentUser(recipientEmail))
                .thenThrow(new UserNotFoundException("Recipient not found"));

        assertThrows(UserNotFoundException.class, () ->
                chatMessageService.markMessagesAsRead(senderEmail, recipientEmail)
        );

        verify(chatMessageRepository, never()).markAllAsRead(any(), any());
    }

    @Test
    @DisplayName("markMessagesAsRead: Bad Path - Database failure")
    void markMessagesAsRead_DatabaseError() {
        String senderEmail = "sender@test.pl";
        String recipientEmail = "recipient@test.pl";

        UsersEntity sender = new UsersEntity();
        UsersEntity recipient = new UsersEntity();

        when(usersService.getCurrentUser(senderEmail)).thenReturn(sender);
        when(usersService.getCurrentUser(recipientEmail)).thenReturn(recipient);

        doThrow(new RuntimeException("Database connection lost"))
                .when(chatMessageRepository).markAllAsRead(sender, recipient);

        assertThrows(RuntimeException.class, () ->
                chatMessageService.markMessagesAsRead(senderEmail, recipientEmail)
        );
    }
    @Test
    @DisplayName("getTotalUnreadCount: Success - should return unread count for existing user")
    void getTotalUnreadCount_HappyPath() {
        String email = "adrian@test.pl";
        UsersEntity me = new UsersEntity();
        me.setEmail(email);
        long expectedCount = 10L;

        when(usersService.getCurrentUser(email)).thenReturn(me);
        when(chatMessageRepository.countTotalUnread(me)).thenReturn(expectedCount);

        UnreadCountDto expectedDto = new UnreadCountDto(expectedCount);
        when(chatSummaryDtoMapper.toUnreadCountDto(expectedCount)).thenReturn(expectedDto);

        UnreadCountDto result = chatMessageService.getTotalUnreadCount(email);

        assertThat(result.totalUnread()).isEqualTo(10L);
        verify(chatMessageRepository, times(1)).countTotalUnread(me);
        verify(chatSummaryDtoMapper, times(1)).toUnreadCountDto(expectedCount);
    }

    @Test
    @DisplayName("getTotalUnreadCount: Success - should return zero when no unread messages")
    void getTotalUnreadCount_Empty() {
        String email = "clean@test.pl";
        UsersEntity me = new UsersEntity();
        me.setEmail(email);

        when(usersService.getCurrentUser(email)).thenReturn(me);
        when(chatMessageRepository.countTotalUnread(me)).thenReturn(0L);
        when(chatSummaryDtoMapper.toUnreadCountDto(0L)).thenReturn(new UnreadCountDto(0L));

        UnreadCountDto result = chatMessageService.getTotalUnreadCount(email);

        assertThat(result.totalUnread()).isZero();
    }

    @Test
    @DisplayName("getTotalUnreadCount: Bad Path - should throw exception when user email is invalid")
    void getTotalUnreadCount_UserNotFound() {
        String fakeEmail = "unknown@test.pl";
        when(usersService.getCurrentUser(fakeEmail))
                .thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () ->
                chatMessageService.getTotalUnreadCount(fakeEmail)
        );

        verify(chatMessageRepository, never()).countTotalUnread(any());
    }
}
