package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto.RefreshTokenRequest;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import io.github.adrian.wieczorek.local_trade.service.user.dto.RegisterUsersDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
@EnableWebSecurity
public class LogoutIntegrationTests extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static final RabbitMQContainer rabbitContainer = new RabbitMQContainer("rabbitmq:3.12-management");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitContainer::getAdminPassword);
    }

    @Test
    @Transactional
    void shouldBlacklistAccessTokenAfterLogout() throws Exception {
        RegisterUsersDto registerDto = new RegisterUsersDto();
        registerDto.setName("Jan");
        registerDto.setEmail("jan@test.pl");
        registerDto.setPassword("Haslo123!");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk());

        LoginDto loginDto = new LoginDto("jan@test.pl", "Haslo123!");

        var loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String accessToken = loginResponse.getToken();

        // Wyciągamy Refresh Token z CIASTECZKA (MockHttpServletResponse)
        var refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");

        // Asercja dla pewności, że ciastko przyszło
        if (refreshTokenCookie == null) {
            throw new RuntimeException("Nie znaleziono ciasteczka refreshToken w odpowiedzi logowania!");
        }

        mockMvc.perform(get("/favorite/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());


        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk());


        mockMvc.perform(get("/favorite/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }
}

