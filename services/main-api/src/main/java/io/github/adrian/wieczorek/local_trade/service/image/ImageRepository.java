package io.github.adrian.wieczorek.local_trade.service.image;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {
  ImageEntity findByImageId(UUID imageId);

  ImageEntity findByAdvertisementEntity(AdvertisementEntity advertisementEntity);

  List<ImageEntity> findAllByAdvertisementEntity(AdvertisementEntity ad);
}
