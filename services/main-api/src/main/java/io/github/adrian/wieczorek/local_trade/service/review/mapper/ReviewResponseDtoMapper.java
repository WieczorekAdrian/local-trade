package io.github.adrian.wieczorek.local_trade.service.review.mapper;

import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewResponseDtoMapper {
  ReviewResponseDto toDto(ReviewEntity reviewEntity);
}
