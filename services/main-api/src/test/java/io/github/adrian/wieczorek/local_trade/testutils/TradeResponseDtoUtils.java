package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TradeResponseDtoUtils {
  UUID reviewId = UUID.randomUUID();
  Long id = 3L;
  TradeStatus status = TradeStatus.PROPOSED;
  BigDecimal proposedPrice = BigDecimal.valueOf(2);
  LocalDateTime createdAt = LocalDateTime.now();
  boolean buyerMarkedCompleted = false;
  boolean sellerMarkedCompleted = false;
  SimpleUserResponseDto buyerSimpleDto = new SimpleUserResponseDtoUtils().build();
  SimpleUserResponseDto sellerSimpleDto = new SimpleUserResponseDtoUtils().build();
  SimpleAdvertisementResponseDto simpleAdvertisementResponseDto =
      new SimpleAdvertisementResponseDtoUtils().build();

  public TradeResponseDtoUtils withId(Long id) {
    this.id = id;
    return this;
  }

  public TradeResponseDtoUtils withStatus(TradeStatus status) {
    this.status = status;
    return this;
  }

  public TradeResponseDtoUtils withProposedPrice(BigDecimal proposedPrice) {
    this.proposedPrice = proposedPrice;
    return this;

  }

  public TradeResponseDtoUtils withCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    return this;

  }

  public TradeResponseDtoUtils withBuyerMarkedCompleted(boolean buyerMarkedCompleted) {
    this.buyerMarkedCompleted = buyerMarkedCompleted;
    return this;
  }

  public TradeResponseDtoUtils withSellerMarkedCompleted(boolean sellerMarkedCompleted) {
    this.sellerMarkedCompleted = sellerMarkedCompleted;
    return this;
  }

  public TradeResponseDtoUtils withBuyerSimpleDto(SimpleUserResponseDto buyerSimpleDto) {
    this.buyerSimpleDto = buyerSimpleDto;
    return this;

  }

  public TradeResponseDtoUtils withSellerSimpleDto(SimpleUserResponseDto sellerSimpleDto) {
    this.sellerSimpleDto = sellerSimpleDto;
    return this;
  }

  public TradeResponseDtoUtils withSimpleAdvertisementResponse(
      SimpleAdvertisementResponseDto simpleAdvertisementResponseDto) {
    this.simpleAdvertisementResponseDto = simpleAdvertisementResponseDto;
    return this;
  }

  public TradeResponseDto build() {
    return new TradeResponseDto(reviewId, id, status, proposedPrice, createdAt,
        buyerMarkedCompleted, sellerMarkedCompleted, buyerSimpleDto, sellerSimpleDto,
        simpleAdvertisementResponseDto);
  }
}
