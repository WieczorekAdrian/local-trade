package io.github.adrian.wieczorek.local_trade.service.advertisement.mapper;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class AdvertisementMapperToAdvertisementUpdateDto {
  public static AdvertisementUpdateDto toDto(AdvertisementEntity advertisementEntity) {
    return new AdvertisementUpdateDto(advertisementEntity.getPrice(),
        advertisementEntity.getTitle(), advertisementEntity.getDescription(),
        advertisementEntity.getLocation(), advertisementEntity.getImage());

  }

}
