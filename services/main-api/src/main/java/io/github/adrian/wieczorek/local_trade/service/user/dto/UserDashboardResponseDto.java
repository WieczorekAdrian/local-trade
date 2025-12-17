package io.github.adrian.wieczorek.local_trade.service.user.dto;

import java.util.UUID;

public record UserDashboardResponseDto(String email,
                                       int ratingCount,
                                       double averageRating,
                                       String role,
                                       UUID userId,
                                       String name)  {
}
