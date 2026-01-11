package io.github.adrian.wieczorek.local_trade.service.advertisement.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FavoriteAdvertisementDto(UUID advertisementId, String title, BigDecimal price, String MainImage) {
}
