package io.github.adrian.wieczorek.local_trade.service.trade.service;

import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.mapper.TradeResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class TradeFinder {

  private final UsersService usersService;
  private final TradeRepository tradeRepository;
  private final TradeResponseDtoMapper tradeResponseDtoMapper;

  public List<TradeResponseDto> getAllMyTrades(UserDetails userDetails) {
    UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());
    List<TradeEntity> tradeEntities = tradeRepository.findAllByBuyerOrSeller(user, user);

    return tradeEntities.stream().map(tradeResponseDtoMapper::tradeToTradeResponseDto).toList();

  }
}
