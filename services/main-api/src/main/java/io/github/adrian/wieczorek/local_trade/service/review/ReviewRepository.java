package io.github.adrian.wieczorek.local_trade.service.review;

import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
  Optional<ReviewEntity> findByReviewedUserOrReviewer(UsersEntity user, UsersEntity user1);

  List<ReviewEntity> findAllByReviewedUserOrReviewer(UsersEntity user, UsersEntity user1);

  boolean existsByTradeEntityAndReviewer(TradeEntity tradeEntity, UsersEntity reviewer);

  Optional<ReviewEntity> findByReviewId(UUID reviewId);

  List<ReviewEntity> findAllByReviewedUser(UsersEntity user);
}
