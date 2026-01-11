package io.github.adrian.wieczorek.local_trade.service.trade.dto;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import jakarta.validation.constraints.NotNull;

public record TradeStatusRequestDto(@NotNull TradeStatus tradeStatus) {
}
