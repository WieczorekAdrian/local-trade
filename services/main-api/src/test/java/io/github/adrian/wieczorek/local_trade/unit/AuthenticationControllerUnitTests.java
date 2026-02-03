package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.controller.AuthenticationController;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.service.RefreshTokenService;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerUnitTests {

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private AuthenticationController authenticationController;

  @Test
  void refreshToken_ShouldReturnUnauthorized_WhenTokenIsNull() {
    String token = null;

    ResponseEntity<Void> response = authenticationController.refreshToken(token);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verifyNoInteractions(refreshTokenService);
  }

  @Test
  void refreshToken_ShouldReturnNewTokens_WhenTokenIsValidAndSecure() {
    String oldRefreshToken = "old-refresh-token-123";
    String newAccessToken = "new-access-token-jwt";
    String newRefreshToken = "new-refresh-token-uuid";

    LoginResponse mockResponse = new LoginResponse();
    mockResponse.setToken(newAccessToken);
    mockResponse.setRefreshToken(newRefreshToken);

    ReflectionTestUtils.setField(authenticationController, "isCookieSecure", true);

    when(refreshTokenService.generateNewTokenFromRefresh(any())).thenReturn(mockResponse);

    ResponseEntity<Void> response = authenticationController.refreshToken(oldRefreshToken);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
    assertNotNull(cookies);
    assertEquals(2, cookies.size());

    String accessCookie = cookies.get(0);
    assertTrue(accessCookie.contains("accessToken=" + newAccessToken));
    assertTrue(accessCookie.contains("HttpOnly"));
    assertTrue(accessCookie.contains("Secure"));
    assertTrue(accessCookie.contains("SameSite=None"));
    assertTrue(accessCookie.contains("Max-Age=900"));

    String refreshCookie = cookies.get(1);
    assertTrue(refreshCookie.contains("refreshToken=" + newRefreshToken));
    assertTrue(refreshCookie.contains("Path=/auth/refreshToken"));
  }

  @Test
  void refreshToken_ShouldSetLaxSameSite_WhenInsecureMode() {
    String oldRefreshToken = "valid-token";
    LoginResponse mockResponse = new LoginResponse();
    mockResponse.setToken("access");
    mockResponse.setRefreshToken("refresh");

    ReflectionTestUtils.setField(authenticationController, "isCookieSecure", false);

    when(refreshTokenService.generateNewTokenFromRefresh(any())).thenReturn(mockResponse);

    ResponseEntity<Void> response = authenticationController.refreshToken(oldRefreshToken);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    String cookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertNotNull(cookieHeader);

    assertTrue(cookieHeader.contains("SameSite=Lax"));
    assertFalse(cookieHeader.contains("; Secure"));
  }
}
