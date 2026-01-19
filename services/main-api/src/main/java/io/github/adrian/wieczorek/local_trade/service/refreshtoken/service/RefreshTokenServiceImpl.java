package io.github.adrian.wieczorek.local_trade.service.refreshtoken.service;

import io.github.adrian.wieczorek.local_trade.exceptions.UserLogOutException;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto.RefreshTokenRequest;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.RefreshTokenEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.RefreshTokenRepository;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;

  @Value("${security.jwt.refresh-token.expiration}")
  private long refreshTokenExpirationMs;

  @Override
  @Transactional
  public RefreshTokenEntity createRefreshToken(UsersEntity user) {
    RefreshTokenEntity refreshTokenEntity =
        RefreshTokenEntity.builder().usersEntity(user).token(UUID.randomUUID().toString())
            .expires(Instant.now().plusMillis(refreshTokenExpirationMs)).build();
    return refreshTokenRepository.save(refreshTokenEntity);
  }

  @Override
  @Transactional
  public LoginResponse generateNewTokenFromRefresh(RefreshTokenRequest refreshTokenRequest) {
    String requestToken = refreshTokenRequest.getToken();

    return refreshTokenRepository.findByToken(requestToken).map(this::verifyExpiry)
        .map(refreshToken -> {

          UsersEntity user = refreshToken.getUsersEntity();

          refreshTokenRepository.delete(refreshToken);

          RefreshTokenEntity newRefreshToken = createRefreshToken(user);

          String newAccessToken = jwtService.generateToken(user);

          log.info("Rotated refresh token for user: {}", user.getEmail());

          return LoginResponse.builder().token(newAccessToken)
              .refreshToken(newRefreshToken.getToken()).build();
        }).orElseThrow(() -> new UserLogOutException("Refresh token not found or revoked"));
  }

  @Override
  public RefreshTokenEntity verifyExpiry(RefreshTokenEntity token) {
    if (token.getExpires().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new UserLogOutException("Refresh token was expired. Please make a new sign in request");
    }
    return token;
  }

  @Override
  @Transactional
  public void revokeRefreshToken(String token) {
    refreshTokenRepository.deleteByToken(token);
  }
}
