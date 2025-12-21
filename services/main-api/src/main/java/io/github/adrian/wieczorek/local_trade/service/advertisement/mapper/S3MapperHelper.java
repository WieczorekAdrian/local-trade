package io.github.adrian.wieczorek.local_trade.service.advertisement.mapper;

import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3MapperHelper {

    private final S3Service s3Service;

    @Named("mapImagesToUrls")
    public List<String> toFullUrls(List<ImageEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(img -> s3Service.generatePresignedUrl(img.getKey(), Duration.ofHours(1)))
                .toList();
    }

    @Named("mapImagesToThumbnailUrls")
    public List<String> toThumbnailUrls(List<ImageEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(img -> s3Service.generatePresignedUrl(img.getThumbnailKey(),Duration.ofHours(1)))
                .toList();
    }
}