package io.github.adrian.wieczorek.local_trade.service.trade;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<TradeEntity, Long> {
  boolean existsByAdvertisementEntityAndBuyer(AdvertisementEntity advertisementEntity,
      UsersEntity buyer);

  List<TradeEntity> findAllByBuyerOrSeller(UsersEntity user, UsersEntity userAgain);

  Optional<TradeEntity> findByBuyerAndSeller(UsersEntity user, UsersEntity userAgain);

  Optional<TradeEntity> findByTradeId(UUID tradeId);
}
