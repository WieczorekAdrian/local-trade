package io.github.adrian.wieczorek.local_trade.service.advertisement.mapper;

import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;



@Mapper(componentModel = "spring", uses = S3MapperHelper.class)
public interface AdvertisementDtoMapper {

    @Mapping(source = "categoryEntity", target = "categoryId", qualifiedByName = "categoryToId")
    @Mapping(source = "imageEntities", target = "imageUrls", qualifiedByName = "mapImagesToUrls")
    @Mapping(source = "imageEntities", target = "thumbnailUrls", qualifiedByName = "mapImagesToThumbnailUrls")
    @Mapping(target = "sellerId", source = "user.userId")
    @Mapping(target = "sellerEmail", source = "user.email")
    ResponseAdvertisementDto toResponseAdvertisementDto(AdvertisementEntity advertisementEntity);

    @Named("categoryToId")
    default Integer categoryToId(CategoryEntity categoryEntity) {
        return categoryEntity != null ? categoryEntity.getId() : null;
    }
}