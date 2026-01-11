package io.github.adrian.wieczorek.notifications.service.infrastructure;

import io.github.adrian.wieczorek.dtos.NotificationEvent;

public interface NotificationHandler {
  void handle(NotificationEvent notificationEvent);

  boolean supports(String eventType);
}
