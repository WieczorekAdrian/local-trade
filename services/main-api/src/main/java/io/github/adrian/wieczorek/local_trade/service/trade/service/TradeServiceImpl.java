package io.github.adrian.wieczorek.local_trade.service.trade.service;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.trade.mapper.TradeResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {

  private final AdvertisementService advertisementService;
  private final TradeRepository tradeRepository;
  private final TradeResponseDtoMapper tradeResponseDtoMapper;
  private final UsersService usersService;

  @Transactional
  @Override
  public TradeResponseDto tradeInitiation(UserDetails userDetails,
      TradeInitiationRequestDto tradeInitiationRequestDto) {
    UsersEntity buyer = usersService.getCurrentUser(userDetails.getUsername());
    AdvertisementEntity advertisementEntity =
        advertisementService.getCurrentAdvertisement(tradeInitiationRequestDto.advertisementId());

    UsersEntity seller = advertisementEntity.getUser();

    BigDecimal newPrice = Optional.ofNullable(tradeInitiationRequestDto.proposedPrice())
        .orElse(advertisementEntity.getPrice());

    if (tradeRepository.existsByAdvertisementEntityAndBuyer(advertisementEntity, buyer)) {
      throw new IllegalArgumentException("Trade already exists");
    }
    if (seller.getId().equals(buyer.getId())) {
      throw new IllegalArgumentException("Seller and buyer are the same");
    }
    TradeEntity newTradeEntity = TradeEntity.builder().seller(seller).buyer(buyer)
        .proposedPrice(newPrice).advertisementEntity(advertisementEntity)
        .status(TradeStatus.PROPOSED).sellerLeftReview(false).buyerLeftReview(false).build();
    tradeRepository.save(newTradeEntity);
    return tradeResponseDtoMapper.tradeToTradeResponseDto(newTradeEntity);
  }

  @Transactional
  @Override
  public TradeResponseDto tradeIsComplete(UserDetails userDetails, Long tradeId) {
    UsersEntity loggedInUser = usersService.getCurrentUser(userDetails.getUsername());
    TradeEntity tradeEntity = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new EntityNotFoundException("Trade not found"));
    if (!tradeEntity.getStatus().equals(TradeStatus.PROPOSED)) {
      throw new IllegalArgumentException(
          "Trade is NOT PROPOSED, Current status is " + tradeEntity.getStatus());
    }
    boolean isBuyer = loggedInUser.getId().equals(tradeEntity.getBuyer().getId());
    boolean isSeller = loggedInUser.getId().equals(tradeEntity.getSeller().getId());

    if (!isBuyer && !isSeller) {
      throw new IllegalArgumentException("Only buyer and seller can complete this trade");
    }

    if (isBuyer) {
      tradeEntity.setBuyerMarkedCompleted(true);
    } else {
      tradeEntity.setSellerMarkedCompleted(true);
    }
    if (tradeEntity.isBuyerMarkedCompleted() && tradeEntity.isSellerMarkedCompleted()) {
      if (LocalDateTime.now().isAfter(tradeEntity.getCreatedAt().plusHours(1))) {
        tradeEntity.setStatus(TradeStatus.COMPLETED);
      }
    }
    tradeRepository.save(tradeEntity);
    return tradeResponseDtoMapper.tradeToTradeResponseDto(tradeEntity);
  }

  @Transactional
  @Override
  public TradeResponseDto tradeIsCancelled(UserDetails userDetails, Long tradeId) {
    UsersEntity loggedInUser = usersService.getCurrentUser(userDetails.getUsername());
    TradeEntity tradeEntity = tradeRepository.findById(tradeId)
        .orElseThrow(() -> new EntityNotFoundException("Trade not found"));
    Integer buyerId = tradeEntity.getBuyer().getId();
    Integer sellerId = tradeEntity.getSeller().getId();

    boolean isBuyer = loggedInUser.getId().equals(buyerId);
    boolean isSeller = loggedInUser.getId().equals(sellerId);

    if (!isBuyer && !isSeller) {
      throw new SecurityException("User is not a part of this trade and cannot cancel it");
    }
    if (!tradeEntity.getStatus().equals(TradeStatus.PROPOSED)) {
      throw new IllegalArgumentException(
          "Trade is NOT PROPOSED, Current status is " + tradeEntity.getStatus());
    }
    if (LocalDateTime.now().isBefore(tradeEntity.getCreatedAt().plusHours(2))) {
      throw new IllegalArgumentException("Trade is too new to cancel");
    }

    tradeEntity.setStatus(TradeStatus.CANCELLED);
    return tradeResponseDtoMapper.tradeToTradeResponseDto(tradeRepository.save(tradeEntity));
  }

  @Transactional
  @Override
  public TradeResponseDto updateTradeStatus(UserDetails userDetails, Long tradeId,
      TradeStatus tradeStatus) {
    return switch (tradeStatus) {
      case COMPLETED -> this.tradeIsComplete(userDetails, tradeId);
      case CANCELLED -> this.tradeIsCancelled(userDetails, tradeId);
      default -> throw new IllegalArgumentException("Trade status not implemented");
    };
  }

  @Transactional
  @Override
  public TradeEntity getTradeEntityByTradeId(UUID tradeId) {
    log.info("Getting trade entity by trade id {}", tradeId);
    TradeEntity trade = tradeRepository.findByTradeId(tradeId)
        .orElseThrow(() -> new EntityNotFoundException("Trade not found with UUID: " + tradeId));
    log.debug("Trade found with UUID {}", tradeId);
    return trade;
  }

  @Transactional
  @Override
  public TradeEntity saveTrade(TradeEntity trade) {
    log.info("Attempting to save trade with UUID{}", trade.getTradeId());
    tradeRepository.save(trade);
    log.debug("Trade saved with UUID {}", trade.getTradeId());
    return trade;
  }
}
