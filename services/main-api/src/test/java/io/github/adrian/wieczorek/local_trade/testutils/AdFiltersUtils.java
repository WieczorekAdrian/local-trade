package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementFilterDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;

import java.math.BigDecimal;

public class AdFiltersUtils {
  public static AdvertisementFilterDto getAdvertisementFilterDto() {
    return new AdvertisementFilterDto(3, new BigDecimal(3), new BigDecimal(1000), "test location",
        "test title", true);
  }

  public static AdvertisementFilterDto filterByCategory(Integer categoryId) {
    return new AdvertisementFilterDto(categoryId, null, null, null, null, null);
  }

  public static AdvertisementFilterDto filterByTitle(String title) {
    return new AdvertisementFilterDto(null, null, null, null, title, null);
  }

  public static AdvertisementFilterDto filterByTitleAndCategoryAndMaxPrice(String title,
      BigDecimal maxPrice, Integer categoryId) {
    return new AdvertisementFilterDto(categoryId, null, maxPrice, null, title, null);
  }

  public static AdvertisementEntity createAdvertisementWithIndex(CategoryEntity categoryEntity,
      UsersEntity user, int index) {
    return AdvertisementEntity.builder().title("Test Advertisement " + index) // Każdy tytuł będzie
                                                                              // unikalny
        .description("Some description").price(BigDecimal.valueOf(100 + index * 10L))
        .location("location")// Każda
                             // cena
                             // będzie
                             // inna
        .categoryEntity(categoryEntity).user(user).build();
  }
}
