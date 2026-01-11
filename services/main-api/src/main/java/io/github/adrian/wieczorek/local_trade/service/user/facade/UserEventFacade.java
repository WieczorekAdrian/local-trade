package io.github.adrian.wieczorek.local_trade.service.user.facade;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.rabbit.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserEventFacade {

  private final NotificationEventPublisher publisher;

  public void publishUserRegistered(UsersEntity newUser) {
    Map<String, String> contextData =
        Map.of("userName", newUser.getName(), "userEmail", newUser.getEmail());

    NotificationEvent event =
        new NotificationEvent("USER_REGISTERED", newUser.getUserId(), contextData);

    String routingKey = "notification.event.user_registered";

    publisher.publishEvent(event, routingKey);
  }
}
