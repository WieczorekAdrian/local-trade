package io.github.adrian.wieczorek.local_trade.service.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserResponseDto {
  @NotBlank
  @Email
  public String email;
  public String name;
  public String password;
}
