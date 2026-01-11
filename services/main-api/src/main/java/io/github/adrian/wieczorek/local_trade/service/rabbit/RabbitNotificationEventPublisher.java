package io.github.adrian.wieczorek.local_trade.service.rabbit;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.local_trade.configs.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitNotificationEventPublisher implements NotificationEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publishEvent(NotificationEvent event, String routingKey) {
    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);
  }
}
