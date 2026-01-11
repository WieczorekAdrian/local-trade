package io.github.adrian.wieczorek.local_trade.service.review.service;

import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewRepository;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.service.review.mapper.ReviewResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReviewFinder {

  private final ReviewRepository reviewRepository;
  private final UsersService usersService;
  private final ReviewResponseDtoMapper reviewResponseDtoMapper;

  @Transactional(readOnly = true)
  public List<ReviewResponseDto> getAllMyReviews(UserDetails userDetails) {
    log.info("Getting all reviews for user {}", userDetails.getUsername());
    UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());

    List<ReviewEntity> reviewEntities =
        reviewRepository.findAllByReviewedUserOrReviewer(user, user);

    if (reviewEntities.isEmpty()) {
      log.info("No reviews found for user with email {}", userDetails.getUsername());
      return Collections.emptyList();
    }

    log.info("Found {} reviews for user {}", reviewEntities.size(), userDetails.getUsername());
    return reviewEntities.stream().map(reviewResponseDtoMapper::toDto).toList();

  }
}
