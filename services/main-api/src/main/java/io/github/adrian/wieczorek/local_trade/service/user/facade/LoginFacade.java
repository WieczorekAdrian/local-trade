package io.github.adrian.wieczorek.local_trade.service.user.facade;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.RefreshTokenEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import io.github.adrian.wieczorek.local_trade.security.AuthenticationService;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginFacade {

  private final AuthenticationService authenticationService;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  public LoginResponse authenticateAndAssignNewRefreshToken(LoginDto loginDto) {
    UsersEntity authenticatedUser = authenticationService.authenticate(loginDto);
    String jwtToken = jwtService.generateToken(authenticatedUser);
    RefreshTokenEntity refreshTokenEntity =
        refreshTokenService.createRefreshToken(authenticatedUser);
    return LoginResponse.builder().refreshToken(refreshTokenEntity.getToken()).token(jwtToken)
        .expiresIn(jwtService.getExpirationTime()).build();
  }
}
