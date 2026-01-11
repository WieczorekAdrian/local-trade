package io.github.adrian.wieczorek.local_trade.service.user.service;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.user.dto.AdminUserViewDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserDashboardResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.mapper.AdminUserViewDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.mapper.UserDashboardResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UsersFinder {

  private final UsersRepository usersRepository;
  private final AdminUserViewDtoMapper adminUserViewDtoMapper;
  private final UserDashboardResponseMapper userDashboardResponseMapper;

  public List<AdminUserViewDto> allUsers() {
    log.info("Finding all users");
    return usersRepository.findAll().stream().map(adminUserViewDtoMapper::toAdminUserViewDto)
        .toList();
  }

  public UserDashboardResponseDto getLoggedInUser(String email) {
    log.info("Finding user with email {}", email);
    UsersEntity currentUser = usersRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User with email:" + email + "Not Found"));
    log.debug("User with email {} found", email);
    return userDashboardResponseMapper.toDto(currentUser);
  }
}
