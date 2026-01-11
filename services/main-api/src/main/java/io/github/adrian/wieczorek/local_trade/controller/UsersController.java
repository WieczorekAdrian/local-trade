package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.user.dto.UpdateUserDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserDashboardResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersFinder;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

  private final UsersService usersService;
  private final UsersFinder usersFinder;

  @GetMapping("/me")
  public ResponseEntity<UserDashboardResponseDto> getLoggedInUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    if (userDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return ResponseEntity.ok(usersFinder.getLoggedInUser(userDetails.getUsername()));
  }

  @PutMapping("/me")
  public ResponseEntity<UserResponseDto> updateCurrentUser(@RequestBody UpdateUserDto updateUserDto,
      @AuthenticationPrincipal UserDetails currentUser) {
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String email = currentUser.getUsername();
    UserResponseDto updatedUser = usersService.updateCurrentUser(updateUserDto, email);
    return ResponseEntity.ok(updatedUser);
  }
}
