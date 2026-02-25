package io.github.adrian.wieczorek.notifications.service.business;

import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;
import io.github.adrian.wieczorek.notifications.service.infrastructure.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage; // Ważny import
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${user.profile.link: placeholder}")
  private String userProfileLink;
  @Value("${spring.mail.username: placeholder}")
  private String fromEmail;
  @Value("${advertisementEntity.link: placeholder}")
  private String advertisementUrl;

  @Override
  public void sendAdvertIsAddedEmail(String toEmail, String userName, String adId, String adTitle) {

    Context context = new Context();
    context.setVariable("userName", userName);
    context.setVariable("advertisementUrl", advertisementUrl);
    context.setVariable("adId", adId);
    context.setVariable("adTitle", adTitle);

    String htmlBody = templateEngine.process("advert-created-email", context);

    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

      try {
        helper.setFrom(fromEmail, "Local Trade Team");
      } catch (UnsupportedEncodingException e) {
        helper.setFrom(fromEmail);
      }
      helper.setTo(toEmail);
      helper.setSubject("Your advertisement is ready and up " + userName + "!");

      helper.setText(htmlBody, true);

      mailSender.send(mimeMessage);

      log.info("Successfully sent email with added advert {}", toEmail);

    } catch (MessagingException | MailException e) {
      log.error("Error when sending email to: with error: {}: {}", toEmail, e.getMessage());
      throw new EmailNotSendException("Error when sending email " + toEmail, e);
    }
  }

  @Override
  public void sendWelcomeEmail(String toEmail, String userName) {
    Context context = new Context();
    context.setVariable("userName", userName);
    context.setVariable("userProfileLink", userProfileLink);
    String htmlBody = templateEngine.process("user-registered", context);

    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
      try {
        helper.setFrom(fromEmail, "Local Trade Team");
      } catch (UnsupportedEncodingException e) {
        helper.setFrom(fromEmail);
      }
      helper.setTo(toEmail);
      helper.setSubject("Welcome to Local Trade " + userName + "!");
      helper.setText(htmlBody, true);
      mailSender.send(mimeMessage);
      log.info("Successfully sent welcome email to: {}", toEmail);
    } catch (MessagingException | MailException e) {
      log.error("Error when sending welcoming email to: with error: {}: {}", toEmail,
          e.getMessage());
      throw new EmailNotSendException("Error when sending email " + toEmail, e);
    }
  }
}
