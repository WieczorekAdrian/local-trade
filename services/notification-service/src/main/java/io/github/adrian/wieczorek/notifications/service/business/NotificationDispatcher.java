package io.github.adrian.wieczorek.notifications.service.business;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.service.infrastructure.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

  private final List<NotificationHandler> handlers;

  public void dispatch(NotificationEvent notificationEvent) {
    if (notificationEvent == null || notificationEvent.getEventType() == null) {
      log.error("Notification event is null or event type is null");
      return;
    }

    String eventType = notificationEvent.getEventType();

    for (NotificationHandler handler : handlers) {
      if (handler.supports(eventType)) {
        log.info("Found handler for {} redirecting to {}", eventType, handler.getClass().getName());
        handler.handle(notificationEvent);
        return;
      }
    }
    log.error("Notification event is  null {}", notificationEvent.getEventType());
  }
}
