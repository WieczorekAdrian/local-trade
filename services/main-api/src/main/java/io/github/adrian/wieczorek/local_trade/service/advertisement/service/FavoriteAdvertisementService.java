package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface FavoriteAdvertisementService {
  @Transactional
  void addFavoriteAdvertisement(String email, UUID advertisementId);

  @Transactional
  void deleteFavoriteAdvertisement(String email, UUID advertisementId);

}
