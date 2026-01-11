package io.github.adrian.wieczorek.local_trade.service.image.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageRepository;
import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.image.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class S3Finder {

  private final AdvertisementService advertisementService;
  private final ImageRepository imageRepository;
  private final S3Service s3Service;

  @Transactional(readOnly = true)
  public List<ImageDto> listFiles(UUID advertisementId) {
    log.info("Attempting to fetch images for advertisement {}", advertisementId);
    AdvertisementEntity ad = advertisementService.getCurrentAdvertisement(advertisementId);
    log.debug("Advertisement found with id: {}", ad.getAdvertisementId());
    List<ImageEntity> imageEntities = imageRepository.findAllByAdvertisementEntity(ad);
    log.debug("Found {} images for advertisement", imageEntities.size());
    return imageEntities.stream().map(image -> {

      String thumbnailKey = image.getKey().replace("full/", "thumbnail/");

      image.setUrl(s3Service.generatePresignedUrl(image.getKey(), Duration.ofHours(1)));

      image.setThumbnailUrl(s3Service.generatePresignedUrl(thumbnailKey, Duration.ofHours(1)));

      return ImageMapper.ImagetoImageDto(image);
    }).toList();
  }
}
