
package io.github.adrian.wieczorek.local_trade.integration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class SharedContainerInitializer {

  // Definiujemy stały, współdzielony kontener.
  protected static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
      new PostgreSQLContainer<>("postgres:15").withDatabaseName("testdb").withUsername("test")
          .withPassword("test");

  protected static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine")).withExposedPorts(6379);

  // Ten blok statyczny uruchomi kontener raz, przed uruchomieniem pierwszego
  // testu.
  static {
    POSTGRES_CONTAINER.start();
    REDIS_CONTAINER.start();
  }
}
