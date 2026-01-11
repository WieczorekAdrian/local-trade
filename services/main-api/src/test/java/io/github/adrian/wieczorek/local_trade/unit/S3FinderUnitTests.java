package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageRepository;
import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Finder;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FinderUnitTests {

  @InjectMocks
  S3Finder s3Finder;

  @Mock
  AdvertisementService advertisementService;

  @Mock
  ImageRepository imageRepository;

  @Mock
  S3Service s3Service;

  private UUID advertisementId;
  private AdvertisementEntity advertisementEntity;
  private ImageEntity imageEntity;

  @BeforeEach
  void setUp() {
    advertisementId = UUID.randomUUID();
    advertisementEntity = new AdvertisementEntity();
    advertisementEntity.setAdvertisementId(advertisementId);

    imageEntity = new ImageEntity();
    imageEntity.setId(1);
    imageEntity.setKey("full/cat.jpg");
    imageEntity.setSize(1024L);
  }

  @Test
  void listFiles_whenImagesExist_returnsDtosWithPresignedUrls() {
    when(advertisementService.getCurrentAdvertisement(advertisementId))
        .thenReturn(advertisementEntity);

    when(imageRepository.findAllByAdvertisementEntity(advertisementEntity))
        .thenReturn(List.of(imageEntity));

    String fullUrl = "http://s3/full/cat.jpg?token=abc";
    String thumbUrl = "http://s3/thumbnail/cat.jpg?token=abc";

    when(s3Service.generatePresignedUrl(eq("full/cat.jpg"), any(Duration.class)))
        .thenReturn(fullUrl);

    when(s3Service.generatePresignedUrl(eq("thumbnail/cat.jpg"), any(Duration.class)))
        .thenReturn(thumbUrl);

    List<ImageDto> result = s3Finder.listFiles(advertisementId);

    Assertions.assertFalse(result.isEmpty());
    Assertions.assertEquals(1, result.size());

    ImageDto dto = result.get(0);

    Assertions.assertEquals(fullUrl, dto.url());
    Assertions.assertEquals(thumbUrl, dto.thumbnailUrl());

    verify(s3Service, times(2)).generatePresignedUrl(anyString(), any());
  }

  @Test
  void listFiles_whenNoImages_returnsEmptyList() {
    when(advertisementService.getCurrentAdvertisement(advertisementId))
        .thenReturn(advertisementEntity);
    when(imageRepository.findAllByAdvertisementEntity(advertisementEntity))
        .thenReturn(Collections.emptyList());

    List<ImageDto> result = s3Finder.listFiles(advertisementId);

    Assertions.assertTrue(result.isEmpty());
    verifyNoInteractions(s3Service);
  }

  @Test
  void listFiles_whenAdNotFound_throwsException() {
    when(advertisementService.getCurrentAdvertisement(advertisementId))
        .thenThrow(new EntityNotFoundException("Ad not found"));

    Assertions.assertThrows(EntityNotFoundException.class,
        () -> s3Finder.listFiles(advertisementId));

    verifyNoInteractions(imageRepository);
    verifyNoInteractions(s3Service);
  }
}
