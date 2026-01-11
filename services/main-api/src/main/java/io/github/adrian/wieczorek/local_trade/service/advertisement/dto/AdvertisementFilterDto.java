package io.github.adrian.wieczorek.local_trade.service.advertisement.dto;

import java.math.BigDecimal;

public record AdvertisementFilterDto(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String location,
		String title, Boolean active) {
}
