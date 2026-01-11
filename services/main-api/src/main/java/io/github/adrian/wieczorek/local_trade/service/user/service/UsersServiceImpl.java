package io.github.adrian.wieczorek.local_trade.service.user.service;

import io.github.adrian.wieczorek.local_trade.service.user.dto.UpdateUserDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserResponseDto;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.user.mapper.UserMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersServiceImpl implements UsersService {

  private final UsersRepository usersRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public UserResponseDto updateCurrentUser(UpdateUserDto dto, String email) {
    log.info("Updating current user");
    UsersEntity currentUser = this.getCurrentUser(email);
    if (currentUser == null) {
      throw new UserNotFoundException("User not found");
    }
    log.info("Updating user with email {}", currentUser.getEmail());
    currentUser.setPassword(passwordEncoder.encode(dto.getPassword()));
    currentUser.setName(dto.getName());
    currentUser.setEmail(dto.getEmail());
    usersRepository.save(currentUser);
    return UserMapper.toDto(currentUser);
  }

  @Override
  @Transactional
  public UsersEntity getCurrentUser(String email) throws UserNotFoundException {
    log.debug("Finding users by email {}", email);
    UsersEntity user = usersRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    log.debug("Found user {}", user);
    return user;
  }

  @Override
  @Transactional
  public UsersEntity saveUser(UsersEntity user) {
    log.debug("Saving current user {}", user);
    usersRepository.save(user);
    log.debug("Saved user {}", user);
    return user;
  }
}
