package io.github.adrian.wieczorek.local_trade.service.user.service;

import io.github.adrian.wieczorek.local_trade.service.user.dto.UpdateUserDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.transaction.annotation.Transactional;

public interface UsersService {
  @Transactional
  UserResponseDto updateCurrentUser(UpdateUserDto dto, String email);

  @Transactional
  UsersEntity getCurrentUser(String email);

  @Transactional
  UsersEntity saveUser(UsersEntity user);
}
