package io.github.adrian.wieczorek.local_trade.security;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class ApplicationAuditAware implements AuditorAware<String> {

  @Override
  @NonNull
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return Optional.empty();
    }
    return Optional.ofNullable(authentication.getName());
  }
}
