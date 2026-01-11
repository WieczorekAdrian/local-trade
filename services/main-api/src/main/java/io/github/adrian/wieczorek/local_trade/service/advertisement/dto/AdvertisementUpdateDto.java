package io.github.adrian.wieczorek.local_trade.service.advertisement.dto;

import java.math.BigDecimal;

public record AdvertisementUpdateDto(BigDecimal price, String title, String description, String location,
		String image) {
}
