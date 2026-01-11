package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.rabbit.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

  private final NotificationEventPublisher eventPublisher;

  @GetMapping("/hello")
  public String hello() {
    return "hello ";
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/hello1")
  public String hello2() {
    return "hello2";
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/hello2")
  public String hello3() {
    return "hello3";
  }

}
