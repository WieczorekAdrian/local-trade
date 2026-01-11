package io.github.adrian.wieczorek.notifications.service.infrastructure;

import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;

public interface EmailService {
  void sendAdvertIsAddedEmail(String toEmail, String userName, String adId, String adTitle)
      throws EmailNotSendException;

  void sendWelcomeEmail(String toEmail, String userName) throws EmailNotSendException;
}
