package io.github.adrian.wieczorek.local_trade.service.image.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.image.mapper.ImageMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.image.ImageRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final String bucketName = "advertisements";
    private final ImageRepository imageRepository;
    private final S3Presigner s3Presigner;
    private final AdvertisementService advertisementService;
    private final TransactionTemplate transactionTemplate;



    @Override
    public PutObjectRequest putObject(String bucketName, String key, @Nullable String content) {
        return  PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(content)
                .build();
    }

    @Override
    public ImageEntity uploadFile(UUID advertisementId, MultipartFile file) throws IOException {
        AdvertisementEntity ad = advertisementService.getCurrentAdvertisement(advertisementId);

        String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        String key = ad.getId() + "/full/" + fileName;

        byte[] thumbnail = generateThumbnail(file);

        String thumbnailKey = (thumbnail != null) ? ad.getId() + "/thumbnail/" + fileName : null;

        try {
            if (thumbnail != null) {
                PutObjectRequest thumbnailRequest = putObject(bucketName, thumbnailKey, "image/jpeg"); // Thumbnail to zawsze JPG u Ciebie
                s3Client.putObject(thumbnailRequest, RequestBody.fromBytes(thumbnail));
            }

            PutObjectRequest putObjectRequest = putObject(bucketName, key, file.getContentType());
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        } catch (Exception e) {
            if (thumbnailKey != null) silentDelete(thumbnailKey);
            silentDelete(key);
            throw new RuntimeException("S3 Upload failed", e);
        }

        ImageEntity savedEntity;

        try {
            savedEntity = transactionTemplate.execute(status -> {
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setAdvertisementEntity(ad);
                imageEntity.setKey(key);
                imageEntity.setContentType(file.getContentType());
                imageEntity.setSize(file.getSize());

                imageEntity.setThumbnailKey(thumbnailKey);
                imageEntity.setImageId(UUID.randomUUID());

                return imageRepository.save(imageEntity);
            });
        } catch (Exception dbException) {
            log.error("Database save failed. Rolling back transaction", dbException);
            silentDelete(key);
            if (thumbnailKey != null) {
                silentDelete(thumbnailKey);
            }
            throw dbException;
        }

        if (savedEntity != null) {
            savedEntity.setUrl(generatePresignedUrl(key, Duration.ofHours(1)));
            if (thumbnailKey != null) {
                savedEntity.setThumbnailUrl(generatePresignedUrl(thumbnailKey, Duration.ofHours(1)));
            }
        }
        return savedEntity;
    }

    private void silentDelete (String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        }catch (Exception e){
            log.error("Failed to rollback S3 file: {}", key, e);
        }
    }

    @Override
    public void deleteFile(UUID imageId) {

        String[] keysToDelete = transactionTemplate.execute(status -> {
            ImageEntity imageEntity = imageRepository.findByImageId(imageId);
            if (imageEntity == null) return null;

            String k = imageEntity.getKey();
            String tk = imageEntity.getThumbnailKey();

            imageRepository.delete(imageEntity);
            imageRepository.flush();

            return new String[]{k, tk};
        });

        if (keysToDelete != null) {
            silentDelete(keysToDelete[0]);
            silentDelete(keysToDelete[1]);
        }
    }

    @Override
    public byte[] generateThumbnail(MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        if (bufferedImage == null) {
            log.warn("Uploaded file is not a valid image. Skipping thumbnail generation.");
            return null;
        }
        BufferedImage thumb = Scalr.resize(bufferedImage, 150);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(thumb, "jpg", os);
        return os.toByteArray();
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build())
                        .signatureDuration(duration)
                        .build()
        );
        return presignedRequest.url().toString();
    }
}
