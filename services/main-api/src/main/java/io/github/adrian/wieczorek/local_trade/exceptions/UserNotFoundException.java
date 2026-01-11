package io.github.adrian.wieczorek.local_trade.exceptions;

public class UserNotFoundException extends ResourceNotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
