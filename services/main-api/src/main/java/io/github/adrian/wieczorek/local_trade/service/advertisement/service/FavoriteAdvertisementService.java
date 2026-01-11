package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

public interface FavoriteAdvertisementService {
  @Transactional(readOnly = true)
  Set<FavoriteAdvertisementDto> getFavoriteAdvertisements(UserDetails userDetails);

  @Transactional
  void addFavoriteAdvertisement(UserDetails userDetails, UUID advertisementId);

  @Transactional
  void deleteFavoriteAdvertisement(UserDetails userDetails, UUID advertisementId);

  @Transactional(readOnly = true)
  UsersEntity getUser(UserDetails userDetails);
}
