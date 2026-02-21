package io.github.adrian.wieczorek.local_trade.service.refreshtoken;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RefreshTokenEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String token;
  private Instant expires;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "users_id", nullable = false)
  private UsersEntity usersEntity;

}
