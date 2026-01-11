package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.FavoriteAdvertisementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteAdvertisementMapperTests {

  private FavoriteAdvertisementMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(FavoriteAdvertisementMapper.class);
  }

  @Test
  @DisplayName("Should correctly map a full AdvertisementEntity to DTO")
  void shouldMapEntityToDto() {
    AdvertisementEntity entity = new AdvertisementEntity();
    UUID id = UUID.randomUUID();
    BigDecimal price = new BigDecimal("150.99");
    entity.setAdvertisementId(id);
    entity.setTitle("Test Title");
    entity.setPrice(price);

    FavoriteAdvertisementDto dto = mapper.toDto(entity);

    assertNotNull(dto, "dto should not be null");
    assertEquals(id, dto.advertisementId(), "Advertisement ID should be equal");
    assertEquals("Test Title", dto.title(), "Title should be same");
    assertEquals(price, dto.price(), "Price should be mapped correctly");
  }

  @Test
  @DisplayName("Should return null when the input entity is null")
  void shouldReturnNullWhenEntityIsNull() {
    AdvertisementEntity entity = null;

    FavoriteAdvertisementDto dto = mapper.toDto(entity);

    assertNull(dto, "Should return null when the input entity is null");
  }

  @Test
  @DisplayName("Should correctly map partial data (null fields) from entity")
  void shouldMapPartialNullsCorrectly() {

    AdvertisementEntity entity = new AdvertisementEntity();
    UUID id = UUID.randomUUID();
    entity.setAdvertisementId(id);
    entity.setTitle(null);
    entity.setPrice(null);

    FavoriteAdvertisementDto dto = mapper.toDto(entity);

    assertNotNull(dto);
    assertEquals(id, dto.advertisementId());
    assertNull(dto.title(), "Title should be null");
    assertNull(dto.price(), "Price should be null");
  }
}
