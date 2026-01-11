package io.github.adrian.wieczorek.local_trade.security;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.RegisterUsersDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthenticationService {
  @Transactional
  UsersEntity signup(RegisterUsersDto dto);

  @Transactional
  UsersEntity authenticate(LoginDto dto);

  List<String> getAuthenticatedRoles();

  public void logout(String authHeader, String refreshToken);
}
