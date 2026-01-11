package io.github.adrian.wieczorek.local_trade.service.review.dto;

import java.util.UUID;

public record ReviewResponseDto(int rating, String comment, UUID reviewId) {
}
