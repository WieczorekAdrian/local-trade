package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TradeUtils {
  public static TradeEntity createTestTrade(UsersEntity seller, UsersEntity buyer,
      AdvertisementEntity ad) {
    TradeEntity tradeEntity = new TradeEntity();

    tradeEntity.setTradeId(UUID.fromString("11111111-2222-3333-4444-555555555555"));

    tradeEntity.setSeller(seller);
    tradeEntity.setBuyer(buyer);
    tradeEntity.setAdvertisementEntity(ad);

    tradeEntity.setStatus(TradeStatus.PROPOSED);
    tradeEntity.setProposedPrice(new BigDecimal("99.99"));

    tradeEntity.setSellerLeftReview(false);
    tradeEntity.setBuyerLeftReview(false);
    tradeEntity.setSellerMarkedCompleted(false);
    tradeEntity.setBuyerMarkedCompleted(false);

    tradeEntity.setCreatedAt(LocalDateTime.of(2025, 1, 10, 12, 0));
    tradeEntity.setUpdatedAt(LocalDateTime.of(2025, 1, 10, 12, 0));

    return tradeEntity;
  }

}
