package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class AdUtils {

  public static AdvertisementEntity createAdvertisement() {
    BigDecimal price = new BigDecimal("149.99");
    return AdvertisementEntity.builder().id(1).title("test").description("test").image("test")
        .price(price).active(true).location("test").favoritedByUsers(new HashSet<>()).build();

  }

  public static AdvertisementEntity createAdWithUserAndCategoryAutomaticRoleUser() {
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    UsersEntity user = UserUtils.createUserRoleUser();
    BigDecimal price = new BigDecimal("149.99");
    return AdvertisementEntity.builder().title("title test").description("description test")
        .price(price).active(true).location("location test").user(user)
        .categoryEntity(categoryEntity).advertisementId(UUID.randomUUID()).build();
  }

  public static AdvertisementEntity createAdvertisementRoleUserForIntegrationTests(
      CategoryEntity categoryEntity, UsersEntity user) {
    BigDecimal price = new BigDecimal("149.99");
    return AdvertisementEntity.builder().title("title test").description("description test")
        .price(price).active(true).location("location test").user(user)
        .categoryEntity(categoryEntity).advertisementId(UUID.randomUUID()).build();
  }

  public static AdvertisementUpdateDto createAdvertisementUpdateDto() {
    BigDecimal price = new BigDecimal("149.99");
    return new AdvertisementUpdateDto(price, "title update test", "title description test",
        "location test ", "image test");
  }

  public static RequestAdvertisementDto createRequestAdvertisementDto() {
    return new RequestAdvertisementDto(1, BigDecimal.valueOf(150), "test", "test", "test", true,
        "test");

  }

  public static ResponseAdvertisementDto createResponseAdvertisementDto() {
    return new ResponseAdvertisementDto(LocalDateTime.now(), UUID.randomUUID(), UUID.randomUUID(),
        "randomemail@email.pl", 1, BigDecimal.valueOf(150), "test", "test", "test", true, "test",
        new ArrayList<>(), new ArrayList<>());
  }
}
