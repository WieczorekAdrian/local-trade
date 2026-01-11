package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface AdvertisementSecurityService {
  boolean isOwner(Authentication authentication, Integer advertisement);

  boolean isOwner(UserDetails userDetails, UUID advertisementId);
}
