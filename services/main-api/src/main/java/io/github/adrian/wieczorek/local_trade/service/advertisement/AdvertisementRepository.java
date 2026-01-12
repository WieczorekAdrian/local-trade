package io.github.adrian.wieczorek.local_trade.service.advertisement;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<AdvertisementEntity, Integer> {
  List<AdvertisementEntity> findByCategoryEntityId(Integer categoryId);

  Optional<AdvertisementEntity> findByUserAndId(UsersEntity user, Integer advertisementId);

  Page<AdvertisementEntity> findAll(Specification<AdvertisementEntity> specification,
      Pageable pageable);

  Optional<AdvertisementEntity> findByAdvertisementId(UUID advertisementId);

  List<AdvertisementEntity> user(UsersEntity user);

  long countByCategoryEntityId(Integer categoryId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(
      value = "UPDATE advertisement_entity SET active = false WHERE active = true AND created_at < :cutoffDate",
      nativeQuery = true)
  int deactivateExpiredAds(@Param("cutoffDate") LocalDateTime cutoffDate);

  @Query("SELECT a FROM AdvertisementEntity a " + "JOIN a.favoritedByUsers u "
      + "WHERE u.email = :username AND a.active = true")
  Set<AdvertisementEntity> findActiveFavoritesByUsername(@Param("username") String username);
}
