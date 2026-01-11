package io.github.adrian.wieczorek.notifications.unit;

import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;
import io.github.adrian.wieczorek.notifications.service.business.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTests {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private TemplateEngine templateEngine;

  @Mock
  private MimeMessage mockMimeMessage;

  @InjectMocks
  private EmailServiceImpl emailService;

  @Captor
  private ArgumentCaptor<Context> contextCaptor;

  @Captor
  private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

  @BeforeEach
  void setUp() {

    when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);

    ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    ReflectionTestUtils.setField(emailService, "advertisementUrl", "http://example.com/ad/");
    ReflectionTestUtils.setField(emailService, "userProfileLink", "http://example.com/profile/");
  }

  @Test
  void sendAdvertIsAddedEmail_shouldSendEmailSuccessfully() throws Exception {
    // ARRANGE
    String to = "user@test.com";
    String userName = "TestUser";
    String adId = "123";
    String adTitle = "Super Ogłoszenie";
    String htmlBody = "<html>Test</html>";

    when(templateEngine.process(eq("advert-created-email"), any(Context.class)))
        .thenReturn(htmlBody);

    emailService.sendAdvertIsAddedEmail(to, userName, adId, adTitle);

    verify(templateEngine).process(eq("advert-created-email"), contextCaptor.capture());

    Context capturedContext = contextCaptor.getValue();
    assertEquals(userName, capturedContext.getVariable("userName"));
    assertEquals(adId, capturedContext.getVariable("adId"));
    assertEquals(adTitle, capturedContext.getVariable("adTitle"));

    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  void sendAdvertIsAddedEmail_shouldThrowEmailNotSendException_whenSenderFails()
      throws MailException, MessagingException {
    when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");

    doThrow(new MailException("Failed to connect") {}).when(mailSender).send(mockMimeMessage);

    assertThrows(EmailNotSendException.class, () -> {
      emailService.sendAdvertIsAddedEmail("user@test.com", "TestUser", "123", "Tytuł");
    });

    verify(mailSender, times(1)).send(mockMimeMessage);
  }

  @Test
  void sendWelcomeEmail_shouldSendEmailSuccessfully() throws Exception {
    String to = "new@user.com";
    String userName = "NowyUżytkownik";
    String htmlBody = "<html>Witaj!</html>";

    when(templateEngine.process(eq("user-registered"), any(Context.class))).thenReturn(htmlBody);

    emailService.sendWelcomeEmail(to, userName);

    verify(templateEngine).process(eq("user-registered"), contextCaptor.capture());

    Context capturedContext = contextCaptor.getValue();
    assertEquals(userName, capturedContext.getVariable("userName"));
    assertEquals("http://example.com/profile/", capturedContext.getVariable("userProfileLink"));

    verify(mailSender, times(1)).send(mockMimeMessage);
  }
}
