package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.configs.UserChannelInterceptor;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserChannelInterceptorUnitTests {

  @Mock
  private JwtService jwtService;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private MessageChannel messageChannel;

  @InjectMocks
  private UserChannelInterceptor interceptor;

  @Test
  void preSend_ShouldAuthenticateUser_WhenCommandIsConnectAndTicketIsValid() {
    String validTicket = "valid.ws.ticket";
    String email = "user@example.com";

    UsersEntity realUserEntity = UserUtils.createUserRoleUser();
    realUserEntity.setEmail(email);

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("ws-ticket", validTicket);
    accessor.setLeaveMutable(true);

    Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

    when(jwtService.extractUsername(validTicket)).thenReturn(email);
    when(usersRepository.findByEmail(email)).thenReturn(Optional.of(realUserEntity));

    Message<?> result = interceptor.preSend(message, messageChannel);

    assertNotNull(result);

    StompHeaderAccessor resultAccessor =
        StompHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
    assertNotNull(resultAccessor);
    assertNotNull(resultAccessor.getUser());
    assertInstanceOf(UsernamePasswordAuthenticationToken.class, resultAccessor.getUser());

    assertEquals(realUserEntity,
        ((UsernamePasswordAuthenticationToken) resultAccessor.getUser()).getPrincipal());
  }

  @Test
  void preSend_ShouldNotAuthenticate_WhenUserNotFound() {
    String validTicket = "valid.ws.ticket";
    String email = "unknown@example.com";

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.addNativeHeader("ws-ticket", validTicket);
    Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

    when(jwtService.extractUsername(validTicket)).thenReturn(email);
    when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

    Message<?> result = interceptor.preSend(message, messageChannel);

    StompHeaderAccessor resultAccessor =
        StompHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
    assertNotNull(resultAccessor);
    assertNull(resultAccessor.getUser());
  }

  @Test
  void preSend_ShouldIgnore_WhenCommandIsNotConnect() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
    accessor.addNativeHeader("ws-ticket", "some_ticket");
    Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

    Message<?> result = interceptor.preSend(message, messageChannel);

    assertNotNull(result);
    StompHeaderAccessor resultAccessor =
        StompHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
    assertNotNull(resultAccessor);
    assertNull(resultAccessor.getUser());

    verifyNoInteractions(jwtService, usersRepository);
  }

  @Test
  void preSend_ShouldIgnore_WhenTicketHeaderIsMissing() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

    Message<?> result = interceptor.preSend(message, messageChannel);

    assertNull(StompHeaderAccessor.getAccessor(result, StompHeaderAccessor.class).getUser());
    verifyNoInteractions(jwtService, usersRepository);
  }
}
