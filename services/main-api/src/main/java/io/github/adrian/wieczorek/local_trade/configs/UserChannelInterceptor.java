package io.github.adrian.wieczorek.local_trade.configs;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import lombok.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserChannelInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final UsersRepository usersRepository;

  public UserChannelInterceptor(JwtService jwtService, UsersRepository usersRepository) {
    this.jwtService = jwtService;
    this.usersRepository = usersRepository;
  }

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String ticket = accessor.getFirstNativeHeader("ws-ticket");

      if (ticket != null) {
        try {
          String email = jwtService.extractUsername(ticket);
          UserDetails user = usersRepository.findByEmail(email)
              .orElseThrow(() -> new UserNotFoundException("Access Denied"));

          accessor
              .setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } catch (Exception e) {
          System.err.println("Nieważny lub przeterminowany bilet WS: " + e.getMessage());
        }
      }
    }
    return message;
  }
}
