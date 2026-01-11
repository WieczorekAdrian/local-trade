package io.github.adrian.wieczorek.local_trade.service.review.service;

import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ReviewService {
  @Transactional
  ReviewResponseDto postReview(UserDetails userDetails, UUID tradeId,
      ReviewRequestDto reviewRequestDto);

  @Transactional
  void deleteReviewByAdmin(UserDetails userDetails, UUID reviewId);
}
