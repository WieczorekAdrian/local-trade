package io.github.adrian.wieczorek.local_trade.service.image.service;

import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import jakarta.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

public interface S3Service {
  PutObjectRequest putObject(String bucketName, String key, @Nullable String content);

  ImageEntity uploadFile(UUID advertisementId, MultipartFile file) throws IOException;

  @Transactional
  void deleteFile(UUID imageId);

  byte[] generateThumbnail(MultipartFile file) throws IOException;

  String generatePresignedUrl(String key, Duration duration);
}
