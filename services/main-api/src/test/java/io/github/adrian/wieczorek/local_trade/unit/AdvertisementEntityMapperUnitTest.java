package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class AdvertisementEntityMapperUnitTest {
  @Test
  public void updateAdvertisementFromDtoSkipNull_thenReturnUpdatedAdvertisement() {
    AdvertisementUpdateDto dto =
        new AdvertisementUpdateDto(null, "test123", "test123", "test123", "test123");
    AdvertisementEntity advertisementEntity = AdUtils.createAdvertisement();
    BigDecimal price = new BigDecimal("149.99");
    AdvertisementMapper mapper = Mappers.getMapper(AdvertisementMapper.class);
    mapper.updateAdvertisementFromDtoSkipNull(dto, advertisementEntity);

    Assertions.assertEquals("test123", advertisementEntity.getTitle());
    Assertions.assertEquals("test123", advertisementEntity.getDescription());
    Assertions.assertEquals("test123", advertisementEntity.getLocation());
    Assertions.assertEquals("test123", advertisementEntity.getImage());
    Assertions.assertEquals(advertisementEntity.getPrice(), price);
  }
}
