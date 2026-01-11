package io.github.adrian.wieczorek.local_trade.service.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
  @NotBlank
  @Email
  private String email;
  @NotBlank
  private String password;
}
