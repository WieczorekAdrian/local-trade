package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto.RefreshTokenRequest;
import io.github.adrian.wieczorek.local_trade.service.user.dto.RegisterUsersDto;
import io.github.adrian.wieczorek.local_trade.service.user.facade.LoginFacade;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import io.github.adrian.wieczorek.local_trade.security.AuthenticationService;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final RefreshTokenService refreshTokenService;
  private final LoginFacade loginFacade;

  @Value("${isCookieSecure}")
  private boolean isCookieSecure;

  @PostMapping("/signup")
  public ResponseEntity<Void> register(@RequestBody @Valid RegisterUsersDto registerUserDto) {
    authenticationService.signup(registerUserDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<Void> authenticate(@RequestBody @Valid LoginDto loginUserDto) {
    LoginResponse loginResponse = loginFacade.authenticateAndAssignNewRefreshToken(loginUserDto);

    ResponseCookie accessCookie = ResponseCookie.from("accessToken", loginResponse.getToken())
        .httpOnly(true).secure(isCookieSecure).path("/").maxAge(15 * 60)
        .sameSite(isCookieSecure ? "None" : "Lax").build();

    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", loginResponse.getRefreshToken()).httpOnly(true)
            .secure(isCookieSecure).path("/auth/refreshToken").maxAge(7 * 24 * 60 * 60)
            .sameSite(isCookieSecure ? "None" : "Lax").build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).build();
  }

  @PostMapping("/refreshToken")
  public ResponseEntity<Void> refreshToken(
      @CookieValue(value = "refreshToken", required = false) String refreshToken) {

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    LoginResponse response =
        refreshTokenService.generateNewTokenFromRefresh(new RefreshTokenRequest(refreshToken));

    ResponseCookie accessCookie = ResponseCookie.from("accessToken", response.getToken())
        .httpOnly(true).secure(isCookieSecure).sameSite(isCookieSecure ? "None" : "Lax").path("/")
        .maxAge(15 * 60).build();

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
        .httpOnly(true).secure(isCookieSecure).sameSite(isCookieSecure ? "None" : "Lax")
        .path("/auth/refreshToken").maxAge(7 * 24 * 60 * 60).build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).build();
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request,
      @CookieValue(value = "refreshToken", required = false) String refreshToken) {
    String accessToken = (String) request.getAttribute("jwt.token");
    authenticationService.logout(accessToken, refreshToken);

    ResponseCookie cleanAccess =
        ResponseCookie.from("accessToken", "").httpOnly(true).secure(isCookieSecure).path("/")
            .maxAge(0).sameSite(isCookieSecure ? "None" : "Lax").build();

    ResponseCookie cleanRefresh =
        ResponseCookie.from("refreshToken", "").httpOnly(true).secure(isCookieSecure)
            .path("/auth/refreshToken").maxAge(0).sameSite(isCookieSecure ? "None" : "Lax").build();

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cleanAccess.toString())
        .header(HttpHeaders.SET_COOKIE, cleanRefresh.toString()).build();
  }
}
