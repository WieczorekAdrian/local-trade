package io.github.adrian.wieczorek.local_trade.service.rabbit;

import io.github.adrian.wieczorek.dtos.NotificationEvent;

public interface NotificationEventPublisher {

  void publishEvent(NotificationEvent notificationEvent, String routingKey);
}
