package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface AdvertisementService {
  @Transactional
  SimpleAdvertisementResponseDto addAd(RequestAdvertisementDto dto, UserDetails userDetails);

  @Transactional
  AdvertisementUpdateDto changeAdvertisement(AdvertisementUpdateDto dto, UserDetails userDetails,
      Integer advertisementId);

  @Transactional
  void deleteAdvertisement(UserDetails userDetails, Integer advertisementId);

  @Transactional
  AdvertisementEntity getCurrentAdvertisement(UUID advertisementId);
}
