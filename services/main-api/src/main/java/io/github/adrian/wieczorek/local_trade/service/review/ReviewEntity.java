package io.github.adrian.wieczorek.local_trade.service.review;

import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ReviewEntity {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trade_id", nullable = false)
  private TradeEntity tradeEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id", nullable = false)
  private UsersEntity reviewer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_user_id", nullable = false)
  private UsersEntity reviewedUser;

  private UUID reviewId = UUID.randomUUID();

  private Integer rating;
  @Size(max = 500)
  private String comment;
}
