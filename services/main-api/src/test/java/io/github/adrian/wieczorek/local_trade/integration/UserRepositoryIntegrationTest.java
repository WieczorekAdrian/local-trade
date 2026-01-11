package io.github.adrian.wieczorek.local_trade.integration;

import io.github.adrian.wieczorek.local_trade.service.user.dto.UpdateUserDto;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;

import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
public class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

  private final UsersRepository usersRepository;
  private final UsersService usersService;
  @Autowired
  private ChatMessageRepository chatMessageRepository;

  @Autowired
  public UserRepositoryIntegrationTest(UsersRepository usersRepository, UsersService usersService) {
    this.usersRepository = usersRepository;
    this.usersService = usersService;
  }

  @Transactional
  @Test
  void whenSavingUser_thenUserSavedCorrectly() {
    long countBefore = usersRepository.count();
    assertEquals(0, countBefore, "No users should be present before the test");

    UsersEntity user = new UsersEntity();
    user.setEmail("test@test.com");
    user.setPassword("test");
    user.setName("test");

    UsersEntity savedUser = usersRepository.save(user);

    assertNotNull(savedUser, "Saved user should be not null");
    assertTrue(savedUser.getId() > 0, "Id should be greater than 0"); // We check if the id was
                                                                      // generated
    assertEquals("test", savedUser.getName(), "User name should be test");
    assertEquals("test", savedUser.getPassword(), "User password should be test");
  }

  @Test
  @Transactional
  void whenUpdatingUser_thenUserUpdatedCorrectly() {
    UsersEntity user = new UsersEntity();

    user.setEmail("test@test.com");
    user.setPassword("test");
    user.setName("test");
    UsersEntity savedUser = usersRepository.save(user);

    UpdateUserDto updateUserDto = new UpdateUserDto();

    updateUserDto.setEmail("test123@test.com");
    updateUserDto.setPassword("test123");
    updateUserDto.setName("test123");
    usersService.updateCurrentUser(updateUserDto, savedUser.getEmail());

    UsersEntity updatedUser = usersRepository.findById(savedUser.getId()).get();

    assertEquals("test123", updatedUser.getName(), "User name should be updated");
    assertNotEquals("test", updatedUser.getPassword(), "User password should be updated");
    assertEquals("test123@test.com", updatedUser.getEmail(), "User email should be updated");

  }

  @Test
  @Transactional
  void whenUpdatingUser_thenUserUpdatedCorrectly_whenUserNotFound() {
    UpdateUserDto updateUserDto = new UpdateUserDto();
    updateUserDto.setEmail("test@test.com");
    updateUserDto.setPassword("test");
    updateUserDto.setName("test");

    String fakeEmail = "fakeEmail@fakeEmail.com";

    assertThrows(UserNotFoundException.class,
        () -> usersService.updateCurrentUser(updateUserDto, fakeEmail));

  }

}
