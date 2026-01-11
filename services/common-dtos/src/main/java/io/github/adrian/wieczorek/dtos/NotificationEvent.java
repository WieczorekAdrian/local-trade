package io.github.adrian.wieczorek.dtos;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationEvent implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private String eventType;
  private UUID recipientUserId;
  private Map<String, String> contextData;

}
