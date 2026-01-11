package io.github.adrian.wieczorek.local_trade.configs;

import io.github.adrian.wieczorek.local_trade.security.ApplicationAuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfiguration {
  @Bean
  public AuditorAware<String> auditorAware() {
    return new ApplicationAuditAware() {};
  }
}
