package io.github.adrian.wieczorek.local_trade.service.advertisement.mapper;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteAdvertisementMapper {

  @Mapping(source = "advertisementId", target = "advertisementId")
  FavoriteAdvertisementDto toDto(AdvertisementEntity advertisementEntity);
}
