package io.github.adrian.wieczorek.local_trade.service.advertisement.dto;

import java.math.BigDecimal;

public record RequestAdvertisementDto(Integer categoryId, BigDecimal price, String title, String image,
		String description, boolean active, String location) {
	public RequestAdvertisementDto withCategoryId(Integer categoryId) {
		return new RequestAdvertisementDto(categoryId, price, title, image, description, active, location);
	}
}