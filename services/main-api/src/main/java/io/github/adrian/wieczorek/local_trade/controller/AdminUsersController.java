package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.user.dto.AdminUserViewDto;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

  private final UsersFinder usersFinder;

  @GetMapping("/all")
  public ResponseEntity<List<AdminUserViewDto>> allUsers() {
    List<AdminUserViewDto> users = usersFinder.allUsers();
    return ResponseEntity.ok(users);
  }
}
