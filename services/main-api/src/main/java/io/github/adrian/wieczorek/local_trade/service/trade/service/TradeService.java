package io.github.adrian.wieczorek.local_trade.service.trade.service;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface TradeService {
  @Transactional
  TradeResponseDto tradeInitiation(UserDetails userDetails,
      TradeInitiationRequestDto tradeInitiationRequestDto);

  @Transactional
  TradeResponseDto tradeIsComplete(UserDetails userDetails, Long tradeId);

  @Transactional
  TradeResponseDto tradeIsCancelled(UserDetails userDetails, Long tradeId);

  @Transactional
  TradeResponseDto updateTradeStatus(UserDetails userDetails, Long tradeId,
      TradeStatus tradeStatus);

  @Transactional
  TradeEntity getTradeEntityByTradeId(UUID tradeId);

  @Transactional
  TradeEntity saveTrade(TradeEntity tradeEntity);
}
