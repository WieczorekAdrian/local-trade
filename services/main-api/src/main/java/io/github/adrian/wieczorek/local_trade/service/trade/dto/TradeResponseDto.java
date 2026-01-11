package io.github.adrian.wieczorek.local_trade.service.trade.dto;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TradeResponseDto(UUID tradeId, Long id, TradeStatus status, BigDecimal proposedPrice,
		LocalDateTime createdAt, boolean buyerMarkedCompleted, boolean sellerMarkedCompleted,
		SimpleUserResponseDto buyerSimpleDto, SimpleUserResponseDto sellerSimpleDto,
		SimpleAdvertisementResponseDto simpleAdvertisementResponseDto) {
}
