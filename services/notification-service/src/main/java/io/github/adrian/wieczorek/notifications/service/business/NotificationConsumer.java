package io.github.adrian.wieczorek.notifications.service.business;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.configs.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationDispatcher notificationDispatcher;

  @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
  public void handleNotification(NotificationEvent notificationEvent) {
    log.info("Received {} redirecting to dispatcher", notificationEvent);
    notificationDispatcher.dispatch(notificationEvent);
  }
}
