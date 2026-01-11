package io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class RefreshTokenRequest {
  private String token;
}
