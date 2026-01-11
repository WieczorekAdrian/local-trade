package io.github.adrian.wieczorek.local_trade.service.trade;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TradeEntity {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Long id;

  UUID tradeId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "advertisement_id")
  private AdvertisementEntity advertisementEntity;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private UsersEntity seller;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id")
  private UsersEntity buyer;

  @Enumerated(EnumType.STRING)
  private TradeStatus status;
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Digits(integer = 5, fraction = 2)
  private BigDecimal proposedPrice;

  private boolean sellerLeftReview;
  private boolean buyerLeftReview;
  private boolean buyerMarkedCompleted = false;
  private boolean sellerMarkedCompleted = false;
}
