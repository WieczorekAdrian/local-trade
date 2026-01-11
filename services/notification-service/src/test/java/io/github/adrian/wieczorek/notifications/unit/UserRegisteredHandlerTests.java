package io.github.adrian.wieczorek.notifications.unit;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;
import io.github.adrian.wieczorek.notifications.handlers.UserRegisteredHandler;
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
public class UserRegisteredHandlerTests {
  @Mock
  private EmailService emailService;
  @InjectMocks
  private UserRegisteredHandler userRegisteredHandler;

  @Test
  public void whenGettingUserRegisteredHandler_shouldReturnEmailSent() {
    var context = Map.of("userName", "Test User", "userEmail", "Test Email");

    NotificationEvent notificationEvent =
        new NotificationEvent("USER_REGISTERED", UUID.randomUUID(), context);

    userRegisteredHandler.handle(notificationEvent);

    verify(emailService, times(1)).sendWelcomeEmail("Test Email", "Test User");

  }

  @Test
  public void whenGettingUserRegisteredHandlerAndThereIsSmtpError_shouldThrowEmailNotSendException() {
    var context = Map.of("userName", "Test User", "userEmail", "Test Email");

    NotificationEvent notificationEvent =
        new NotificationEvent("USER_REGISTERED", UUID.randomUUID(), context);

    doThrow(new EmailNotSendException("SMTP Error", null)).when(emailService)
        .sendWelcomeEmail("Test Email", "Test User");

    Assertions.assertThrows(EmailNotSendException.class,
        () -> userRegisteredHandler.handle(notificationEvent));

    verify(emailService, times(1)).sendWelcomeEmail("Test Email", "Test User");

  }

}
