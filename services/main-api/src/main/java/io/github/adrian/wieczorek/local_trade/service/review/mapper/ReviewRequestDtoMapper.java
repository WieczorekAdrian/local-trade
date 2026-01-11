package io.github.adrian.wieczorek.local_trade.service.review.mapper;

import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import org.mapstruct.Mapper;

@Mapper
public interface ReviewRequestDtoMapper {
  ReviewRequestDto toDto(ReviewEntity reviewEntity);
}
