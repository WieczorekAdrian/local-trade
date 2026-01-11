package io.github.adrian.wieczorek.local_trade.service.chat;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageEntity {
  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sender_id", nullable = false)
  private UsersEntity sender;

  @ManyToOne
  @JoinColumn(name = "recipient_id", nullable = false)
  private UsersEntity recipient;

  private String content;
  private LocalDateTime timestamp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "advertisement_id")
  private AdvertisementEntity advertisement;

  private boolean isRead = false;

  @PrePersist
  protected void onCreate() {
    this.timestamp = LocalDateTime.now();
  }

}
