package io.github.adrian.wieczorek.local_trade.service.advertisement.mapper;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleAdvertisementDtoMapper {

  SimpleAdvertisementResponseDto advertisementToSimpleDto(AdvertisementEntity advertisementEntity);

}
