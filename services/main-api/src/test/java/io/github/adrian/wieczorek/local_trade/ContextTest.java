package io.github.adrian.wieczorek.local_trade;

import io.github.adrian.wieczorek.local_trade.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
class ContextTest extends AbstractIntegrationTest {

  @Test
  void contextLoads() {}

}
