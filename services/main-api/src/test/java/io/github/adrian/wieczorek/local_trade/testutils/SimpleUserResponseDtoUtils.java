package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;

public class SimpleUserResponseDtoUtils {
  Integer id = 1;
  String name = "buyer";

  public SimpleUserResponseDtoUtils withId(Integer id) {
    this.id = id;
    return this;
  }

  public SimpleUserResponseDtoUtils withName(String name) {
    this.name = name;
    return this;
  }

  public SimpleUserResponseDto build() {
    return new SimpleUserResponseDto(id, name);
  }
}
