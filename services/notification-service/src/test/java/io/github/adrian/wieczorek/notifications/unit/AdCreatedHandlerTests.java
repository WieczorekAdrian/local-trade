package io.github.adrian.wieczorek.notifications.unit;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;
import io.github.adrian.wieczorek.notifications.handlers.AdCreatedHandler;
import io.github.adrian.wieczorek.notifications.service.infrastructure.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdCreatedHandlerTests {
  @Mock
  EmailService emailService;
  @InjectMocks
  AdCreatedHandler adCreatedHandler;

  @Test
  public void whenAdIsCreated_thenHandlerSendsEmail_emailIsSentWithoutErrors() {
    var adId = UUID.randomUUID();
    var adIdToString = adId.toString();
    var recipientUserId = UUID.randomUUID();
    Map<String, String> context = Map.of("userEmail", "test@test.com", "userName", "Test User",
        "adId", adIdToString, "adTitle", "Random Title");
    NotificationEvent event = new NotificationEvent("AD_CREATED", recipientUserId, context);

    adCreatedHandler.handle(event);

    verify(emailService, times(1)).sendAdvertIsAddedEmail("test@test.com", "Test User",
        adIdToString, "Random Title");

  }

  @Test
  public void whenAdIsCreated_thenHandlerSendsEmail_emailIsSentButThrowsException() {
    var adId = UUID.randomUUID();
    var adIdToString = adId.toString();
    var recipientUserId = UUID.randomUUID();
    Map<String, String> context = Map.of("userEmail", "test@test.com", "userName", "Test User",
        "adId", adIdToString, "adTitle", "Random Title");
    NotificationEvent event = new NotificationEvent("AD_CREATED", recipientUserId, context);

    doThrow(new EmailNotSendException("Test error SMTP", null)).when(emailService)
        .sendAdvertIsAddedEmail(anyString(), anyString(), anyString(), anyString());

    Assertions.assertThrows(EmailNotSendException.class, () -> adCreatedHandler.handle(event));

    verify(emailService, times(1)).sendAdvertIsAddedEmail("test@test.com", "Test User",
        adIdToString, "Random Title");

  }

}
