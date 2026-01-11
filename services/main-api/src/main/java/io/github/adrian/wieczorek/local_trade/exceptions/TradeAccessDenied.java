package io.github.adrian.wieczorek.local_trade.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class TradeAccessDenied extends AccessDeniedException {
  public TradeAccessDenied(String message) {
    super(message);
  }
}
