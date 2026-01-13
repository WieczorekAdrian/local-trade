package io.github.adrian.wieczorek.local_trade.service.chat.dto;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;

public record PartnerUnreadCountDto(UsersEntity partner, long count) {
}