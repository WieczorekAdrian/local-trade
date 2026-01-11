package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;

import java.util.UUID;

public class ReviewUtils {
  public static ReviewEntity createTestReview(TradeEntity tradeEntity, UsersEntity reviewer,
      UsersEntity reviewedUser) {
    return ReviewEntity.builder().reviewId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"))

        .tradeEntity(tradeEntity).reviewer(reviewer).reviewedUser(reviewedUser)

        .rating(5).comment("Test comment everything is ok").build();
  }
}
