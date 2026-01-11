package io.github.adrian.wieczorek.notifications.handlers;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.service.infrastructure.EmailService;
import io.github.adrian.wieczorek.notifications.service.infrastructure.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdCreatedHandler implements NotificationHandler {
  private final EmailService emailService;

  @Override
  public void handle(NotificationEvent notificationEvent) {
    log.info("Notification event is being created for  {}", notificationEvent.getEventType());
    String email = notificationEvent.getContextData().get("userEmail");
    String userName = notificationEvent.getContextData().get("userName");
    String adId = notificationEvent.getContextData().get("adId");
    String adTitle = notificationEvent.getContextData().get("adTitle");
    log.info("Sending email to {}", email);
    emailService.sendAdvertIsAddedEmail(email, userName, adId, adTitle);
  }

  @Override
  public boolean supports(String eventType) {
    return "AD_CREATED".equals(eventType);
  }

}
