package io.github.adrian.wieczorek.local_trade.service.advertisement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResponseAdvertisementDto(
        LocalDateTime createdAt,
        UUID advertisementId,
        UUID sellerId,
        String sellerEmail,
        Integer categoryId,
        BigDecimal price,
        String title,
        String image,
        String description,
        boolean active,
        String location,
        List<String> imageUrls,
        List<String> thumbnailUrls
) {

}