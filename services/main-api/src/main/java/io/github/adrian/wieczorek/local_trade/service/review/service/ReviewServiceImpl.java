package io.github.adrian.wieczorek.local_trade.service.review.service;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.exceptions.GlobalConflictException;
import io.github.adrian.wieczorek.local_trade.exceptions.TradeAccessDenied;
import io.github.adrian.wieczorek.local_trade.exceptions.TradeReviewedGlobalConflictException;
import io.github.adrian.wieczorek.local_trade.service.review.mapper.ReviewResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeService;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewRepository;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final UsersService usersService;
  private final ReviewResponseDtoMapper reviewResponseDtoMapper;
  private final TradeService tradeService;

  @Transactional
  @Override
  public ReviewResponseDto postReview(UserDetails userDetails, UUID tradeId,
      ReviewRequestDto reviewRequestDto) {
    log.info("Creating new review");

    UsersEntity loggedInUser = usersService.getCurrentUser(userDetails.getUsername());
    TradeEntity completedTradeEntity = tradeService.getTradeEntityByTradeId(tradeId);
    var seller = completedTradeEntity.getSeller();
    var buyer = completedTradeEntity.getBuyer();

    if (!loggedInUser.equals(buyer) && !loggedInUser.equals(seller)) {
      log.warn("Logged in user is not a part of this trade and can't post this review");
      throw new TradeAccessDenied(
          "Logged in user is not a part of this trade and not able to post this review"
              + userDetails.getUsername());
    }
    if (!completedTradeEntity.getStatus().equals(TradeStatus.COMPLETED)) {
      log.warn("Trade {} is not in completed status", completedTradeEntity.getId());
      throw new GlobalConflictException(
          "Trade " + completedTradeEntity.getId() + " is not in completed status");
    }
    if (reviewRepository.existsByTradeEntityAndReviewer(completedTradeEntity, loggedInUser)) {
      log.warn("User {} has already reviewed this trade {}", loggedInUser.getUsername(), tradeId);
      throw new TradeReviewedGlobalConflictException("You have already reviewed this trade");
    }

    UsersEntity reviewedUser = loggedInUser.equals(buyer) ? seller : buyer;

    log.info("User {} is reviewing user {}", loggedInUser.getUsername(),
        reviewedUser.getUsername());
    var review = ReviewEntity.builder().reviewer(loggedInUser).reviewedUser(reviewedUser)
        .tradeEntity(completedTradeEntity).comment(reviewRequestDto.comment())
        .rating(reviewRequestDto.rating()).build();
    ReviewEntity savedReviewEntity = reviewRepository.save(review);
    log.info("User {} successfully posted review", loggedInUser.getUsername());
    if (loggedInUser.equals(buyer)) {
      completedTradeEntity.setBuyerLeftReview(true);
    } else {
      completedTradeEntity.setSellerLeftReview(true);
    }
    this.updateUserRating(reviewedUser);

    tradeService.saveTrade(completedTradeEntity);

    return reviewResponseDtoMapper.toDto(savedReviewEntity);
  }

  @Transactional
  @Override
  public void deleteReviewByAdmin(UserDetails userDetails, UUID reviewId) {
    log.info("Deleting  review");
    UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());
    log.info("User {} with role {} has been found ", userDetails.getUsername(), user.getRole());
    ReviewEntity reviewEntity = reviewRepository.findByReviewId(reviewId).orElseThrow(
        () -> new EntityNotFoundException("Review with id " + reviewId + " not found"));
    log.info("Review {} with id {} has been found ", reviewEntity, reviewEntity.getId());

    reviewRepository.delete(reviewEntity);
    log.info("User {} successfully deleted review {}", userDetails.getUsername(),
        reviewEntity.getId());

  }

  private void updateUserRating(UsersEntity user) {
    log.info("Updating user rating");
    List<ReviewEntity> reviewEntities = reviewRepository.findAllByReviewedUser(user);

    if (reviewEntities.isEmpty()) {
      log.info("No reviews found for user with id: {}", user.getId());
      user.setAverageRating(0.0);
      user.setRatingCount(0);
    } else {
      log.info("Found : {} reviews for user with users id :  {}", reviewEntities.size(),
          user.getId());
      double ratingSum = reviewEntities.stream().mapToDouble(ReviewEntity::getRating).sum();
      double averageRating = ratingSum / reviewEntities.size();
      double roundedAverage = Math.round(averageRating * 10.0) / 10.0;
      user.setAverageRating(roundedAverage);
      user.setRatingCount(reviewEntities.size());
    }

    usersService.saveUser(user);
    log.info("User {} successfully updated user rating {}", user.getId(), user.getAverageRating());
  }
}
