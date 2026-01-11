package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;

@Component
public class AdUtilsIntegrationTests {
  private final CategoryRepository categoryRepository;
  private final UsersRepository usersRepository;
  private final AdvertisementRepository advertisementRepository;

  public AdUtilsIntegrationTests(CategoryRepository categoryRepository,
      UsersRepository usersRepository, AdvertisementRepository advertisementRepository) {
    this.categoryRepository = categoryRepository;
    this.usersRepository = usersRepository;
    this.advertisementRepository = advertisementRepository;
  }

  public AdvertisementEntity createAdWithUserAndCategoryAutomaticRoleUser(String title,
      String description, BigDecimal price) {
    CategoryEntity categoryEntity =
        categoryRepository.save(CategoryUtils.createCategoryForIntegrationTests());
    UsersEntity user = usersRepository.save(UserUtils.createUserRoleUser());
    return advertisementRepository
        .save(AdvertisementEntity.builder().title(title).description(description).price(price)
            .active(true).location("location test").user(user).categoryEntity(categoryEntity)
            .advertisementId(UUID.randomUUID()).favoritedByUsers(new HashSet<>()).build());
  }

  public AdvertisementEntity createIntegrationAd(UsersEntity user, CategoryEntity categoryEntity) {
    return advertisementRepository
        .save(AdvertisementEntity.builder().title("test").description("test").price(BigDecimal.TEN)
            .active(true).location("test").user(user).categoryEntity(categoryEntity)
            .advertisementId(UUID.randomUUID()).favoritedByUsers(new HashSet<>()).build());
  }
}
