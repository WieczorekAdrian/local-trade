package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.FavoriteAdvertisementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdvertisementFavoriteFinder {

  private final AdvertisementRepository advertisementRepository;
  private final FavoriteAdvertisementMapper favoriteAdvertisementMapper;

  public Set<FavoriteAdvertisementDto> getFavoriteAdvertisements(String email) {
    return advertisementRepository.findActiveFavoritesByUsername(email).stream()
        .map(favoriteAdvertisementMapper::toDto).collect(Collectors.toSet());
  }
}
