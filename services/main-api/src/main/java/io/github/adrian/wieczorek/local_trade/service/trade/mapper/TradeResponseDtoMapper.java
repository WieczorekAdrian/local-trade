package io.github.adrian.wieczorek.local_trade.service.trade.mapper;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TradeResponseDtoMapper {

  @Mapping(source = "buyer", target = "buyerSimpleDto")
  @Mapping(source = "seller", target = "sellerSimpleDto")
  @Mapping(source = "advertisementEntity", target = "simpleAdvertisementResponseDto")
  TradeResponseDto tradeToTradeResponseDto(TradeEntity tradeEntity);

  SimpleUserResponseDto toSimpleUserDto(UsersEntity usersEntity);

  SimpleAdvertisementResponseDto toSimpleAdvertisementResponseDto(
      AdvertisementEntity advertisementEntity);
}
