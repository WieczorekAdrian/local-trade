package io.github.adrian.wieczorek.local_trade.service.image.mapper;

import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;

public class ImageMapper {
  public static ImageDto ImagetoImageDto(ImageEntity imageEntity) {
    return new ImageDto(imageEntity.getImageId(), imageEntity.getUrl(),
        imageEntity.getThumbnailUrl(), imageEntity.getSize(), imageEntity.getContentType());
  }
}
