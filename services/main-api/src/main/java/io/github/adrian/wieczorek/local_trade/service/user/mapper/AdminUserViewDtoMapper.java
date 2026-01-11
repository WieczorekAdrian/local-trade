package io.github.adrian.wieczorek.local_trade.service.user.mapper;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.dto.AdminUserViewDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserViewDtoMapper {
  AdminUserViewDto toAdminUserViewDto(UsersEntity usersEntity);
}
