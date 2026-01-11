package io.github.adrian.wieczorek.local_trade.integration;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
@EnableWebSecurity
public class AdminControllerIntegrationTests extends AbstractIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  UsersRepository usersRepository;

  @Test
  @WithMockUser(value = "testadmin@test.com", roles = "ADMIN")
  @Transactional
  void shouldReturnListOfAllUsers() throws Exception {
    UsersEntity admin = UserUtils.createUserRoleAdmin();
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(admin);
    usersRepository.save(user);

    mockMvc.perform(get("/admin/users/all")).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].role").value("ROLE_ADMIN"))
        .andExpect(jsonPath("$[0].name").value("test admin"))
        .andExpect(jsonPath("$[1].role").value("ROLE_USER"))
        .andExpect(jsonPath("$[1].name").value("test"));
  }

  @Test
  @WithMockUser(value = "testadmin@test.com", roles = "ADMIN")
  @Transactional
  void shouldReturnListOfEmpty() throws Exception {
    mockMvc.perform(get("/admin/users/all")).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @WithMockUser(value = "user@roleuser.com", roles = "USER")
  @Transactional
  void shouldThrowAccessDenied() throws Exception {
    mockMvc.perform(get("/admin/users/all")).andExpect(status().isForbidden());
  }
}
