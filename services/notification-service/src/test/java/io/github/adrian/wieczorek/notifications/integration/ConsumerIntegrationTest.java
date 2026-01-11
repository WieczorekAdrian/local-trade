package io.github.adrian.wieczorek.notifications.integration;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.notifications.configs.RabbitMQConfig;
import io.github.adrian.wieczorek.notifications.exceptions.EmailNotSendException;
import io.github.adrian.wieczorek.notifications.service.infrastructure.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class ConsumerIntegrationTest {

  @Container
  static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer();

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
    registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
  }

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @MockitoBean
  private EmailService emailService;

  private final Map<String, String> context =
      Map.of("userName", "Test User", "userEmail", "Test Email");

  @Test
  void whenAdIsCreated_thenEmailServiceIsCalledAndSent() {
    var adId = UUID.randomUUID().toString();
    Map<String, String> contextData = Map.of("userEmail", "test@example.com", "userName",
        "Jan Testowy", "adId", adId, "adTitle", "Test Advert");
    NotificationEvent event = new NotificationEvent("AD_CREATED", UUID.randomUUID(), contextData);
    String routingKey = "notification.event.ad_created";

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      verify(emailService, times(1)).sendAdvertIsAddedEmail("test@example.com", "Jan Testowy", adId,
          "Test Advert");
    });
  }

  @Test
  void whenAdIsCreated_thenEmailServiceIsCalledAndSent_throwsException() {
    var adId = UUID.randomUUID().toString();
    Map<String, String> contextData = Map.of("userEmail", "test@test.com", "userName",
        "Jan Testowy", "adId", adId, "adTitle", "Test Advert DLQ");
    NotificationEvent event = new NotificationEvent("AD_CREATED", UUID.randomUUID(), contextData);

    doThrow(new EmailNotSendException("Testowy Błąd Wysyłki z Mocka", null)).when(emailService)
        .sendAdvertIsAddedEmail(anyString(), anyString(), anyString(), anyString());

    String routingKey = "notification.event.ad_created";

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

      verify(emailService, times(1)).sendAdvertIsAddedEmail("test@test.com", "Jan Testowy", adId,
          "Test Advert DLQ");
      Object msgFromDLQ = rabbitTemplate.receiveAndConvert(RabbitMQConfig.DLQ_QUEUE, 1000);

      Assertions.assertNotNull(msgFromDLQ);
      Assertions.assertEquals(NotificationEvent.class, msgFromDLQ.getClass());
      NotificationEvent dlqEvent = (NotificationEvent) msgFromDLQ;
      Assertions.assertEquals(adId, dlqEvent.getContextData().get("adId"));
      Assertions.assertEquals("Test Advert DLQ", dlqEvent.getContextData().get("adTitle"));

    });

  }

  @Test
  public void whenUserIsCreated_thenEmailServiceIsCalledAndSent() {

    NotificationEvent event = new NotificationEvent("USER_REGISTERED", UUID.randomUUID(), context);

    String routingKey = "notification.event.user_registered";

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      verify(emailService, times(1)).sendWelcomeEmail("Test Email", "Test User");

    });
  }

  @Test
  public void whenUserIsCreatedButBadEvent_neverSendsEmail() {

    NotificationEvent event = new NotificationEvent("UNKNOWN_EVENT", UUID.randomUUID(), context);

    String routingKey = "notification.event.user_registered";

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      verify(emailService, never()).sendWelcomeEmail("Test Email", "Test User");
    });
  }

  @Test
  public void whenUserIsCreated_thenEmailServiceIsCalledAndSendsEmail_throwsException() {
    NotificationEvent event = new NotificationEvent("USER_REGISTERED", UUID.randomUUID(), context);
    String routingKey = "notification.event.user_registered";
    doThrow(new EmailNotSendException("Test Email", null)).when(emailService)
        .sendWelcomeEmail("Test Email", "Test User");
    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);
    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      verify(emailService, times(1)).sendWelcomeEmail("Test Email", "Test User");
      Object dlqMsg = rabbitTemplate.receiveAndConvert(RabbitMQConfig.DLQ_QUEUE, 1000);
      Assertions.assertNotNull(dlqMsg);
      Assertions.assertEquals(NotificationEvent.class, dlqMsg.getClass());
      NotificationEvent dlqEvent = (NotificationEvent) dlqMsg;
      Assertions.assertEquals("Test Email", dlqEvent.getContextData().get("userEmail"));
      Assertions.assertEquals("Test User", dlqEvent.getContextData().get("userName"));
    });

  }

  @Test
  public void whenAdIsCreated_butEventIsUnknown_neverSendsEmail() {
    var adId = UUID.randomUUID().toString();
    Map<String, String> contextData = Map.of("userEmail", "test@example.com", "userName",
        "Jan Testowy", "adId", adId, "adTitle", "Test Advert");
    NotificationEvent event =
        new NotificationEvent("UNKNOWN_EVENT", UUID.randomUUID(), contextData);
    String routingKey = "notification.event.ad_created";

    rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, routingKey, event);

    await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
      verify(emailService, never()).sendAdvertIsAddedEmail("test@example.com", "Jan Testowy", adId,
          "Test Advert");
    });
  }

}
