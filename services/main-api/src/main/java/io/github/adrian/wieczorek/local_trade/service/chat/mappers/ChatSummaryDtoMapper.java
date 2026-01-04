package io.github.adrian.wieczorek.local_trade.service.chat.mappers;

import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatSummaryDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.UnreadCountDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatSummaryDtoMapper {

    @Mapping(target = "lastMessage", source = "entity.content")
    @Mapping(target = "lastMessageTimestamp", source = "entity.timestamp")
    @Mapping(target = "partnerName", source = "partner.name")
    @Mapping(target = "partnerEmail", source = "partner.email")
    @Mapping(target = "unreadCount", source = "unreadCount")
    ChatSummaryDto toChatSummaryDto(ChatMessageEntity entity, UsersEntity partner, long unreadCount);

    default UnreadCountDto toUnreadCountDto(long totalUnread) {
        return new UnreadCountDto(totalUnread);
    }
}