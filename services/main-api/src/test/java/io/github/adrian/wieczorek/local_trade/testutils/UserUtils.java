package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.UUID;

@SpringBootTest
public class UserUtils {

  public static UsersEntity createUserRoleUser() {
    UsersEntity user = new UsersEntity();
    user.setName("test");
    user.setEmail("test@test.com");
    user.setPassword("password");
    user.setRole("ROLE_USER");
    user.setFavoritedAdvertisementEntities(new HashSet<>());
    return user;
  }

  public static UsersEntity createUserRoleUserBuyer() {
    UsersEntity user = new UsersEntity();
    user.setName("buyer");
    user.setEmail("buyer@test.com");
    user.setPassword("password");
    user.setRole("ROLE_USER");
    user.setFavoritedAdvertisementEntities(new HashSet<>());
    return user;
  }

  public static UsersEntity createUserRoleUserSeller() {
    UsersEntity user = new UsersEntity();
    user.setName("seller");
    user.setEmail("seller@test.com");
    user.setPassword("password");
    user.setRole("ROLE_USER");
    user.setFavoritedAdvertisementEntities(new HashSet<>());
    return user;
  }

  public static UsersEntity createUserRoleUserUnitTestWithUUID() {
    UsersEntity user = new UsersEntity();
    user.setUserId(UUID.randomUUID());
    user.setName("buyer");
    user.setEmail("buyer@test.com");
    user.setPassword("password");
    user.setRole("ROLE_USER");
    user.setFavoritedAdvertisementEntities(new HashSet<>());
    return user;
  }

  public static UsersEntity createUserRoleAdmin() {
    UsersEntity user = new UsersEntity();
    user.setName("test admin");
    user.setEmail("testadmin@test.com");
    user.setPassword("password");
    user.setRole("ROLE_ADMIN");
    return user;
  }

  public static LoginDto createLoginDto(UsersEntity user) {
    LoginDto loginDto = new LoginDto();
    loginDto.setEmail(user.getEmail());
    loginDto.setPassword(user.getPassword());
    return loginDto;
  }
}
