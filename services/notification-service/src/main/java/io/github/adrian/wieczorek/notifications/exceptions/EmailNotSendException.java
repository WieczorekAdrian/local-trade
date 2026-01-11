package io.github.adrian.wieczorek.notifications.exceptions;

public class EmailNotSendException extends RuntimeException {
  public EmailNotSendException(String message, Throwable cause) {
    super(message);
  }
}
