package io.github.adrian.wieczorek.local_trade.configs;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {

  @Bean
  public RedisCacheConfiguration redisCacheConfiguration() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    BasicPolymorphicTypeValidator ptv =
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(SerializationPair.fromSerializer(serializer));
  }
}
