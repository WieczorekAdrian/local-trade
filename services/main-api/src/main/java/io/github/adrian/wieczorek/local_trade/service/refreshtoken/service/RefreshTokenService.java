package io.github.adrian.wieczorek.local_trade.service.refreshtoken.service;

import io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto.RefreshTokenRequest;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.RefreshTokenEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenService {
  @Transactional
  RefreshTokenEntity createRefreshToken(UsersEntity user);

  @Transactional(readOnly = true)
  LoginResponse generateNewTokenFromRefresh(RefreshTokenRequest refreshTokenRequest);

  @Transactional
  RefreshTokenEntity verifyExpiry(RefreshTokenEntity token);

  @Transactional
  void revokeRefreshToken(String token);
}
