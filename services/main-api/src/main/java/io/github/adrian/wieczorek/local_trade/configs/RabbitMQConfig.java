package io.github.adrian.wieczorek.local_trade.configs;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
  public static final String NOTIFICATION_QUEUE = "notification.queue";
  public static final String NOTIFICATION_ROUTING_KEY = "notification.event.#";

  public static final String DLX_EXCHANGE = "notification.exchange.dlx";
  public static final String DLQ_QUEUE = "notification.queue.dlq";

  @Bean
  public TopicExchange notificationExchange() {
    return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
  }

  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable(NOTIFICATION_QUEUE)
        .withArgument("x-dead-letter-exchange", DLX_EXCHANGE).build();
  }

  @Bean
  public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
    return BindingBuilder.bind(notificationQueue).to(notificationExchange)
        .with(NOTIFICATION_ROUTING_KEY);
  }

  @Bean
  public TopicExchange deadLetterExchange() {
    return new TopicExchange(DLX_EXCHANGE, true, false);
  }

  @Bean
  public Queue deadLetterQueue() {
    return new Queue(DLQ_QUEUE, true);
  }

  @Bean
  public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
  }
}
