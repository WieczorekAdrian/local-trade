package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementFilterDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

public interface AdvertisementFilterService {
  Specification<AdvertisementEntity> getSpecification(AdvertisementFilterDto filter);

  @Transactional(readOnly = true)
  Page<ResponseAdvertisementDto> filterAndPageAdvertisements(
      AdvertisementFilterDto advertisementFilterDto, Pageable pageable);
}
