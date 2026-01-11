package io.github.adrian.wieczorek.local_trade.service.advertisement.facade;

import io.github.adrian.wieczorek.dtos.NotificationEvent;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.rabbit.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdvertisementEventFacade {

  private final NotificationEventPublisher publisher;

  public void publishAdCreated(UsersEntity user, AdvertisementEntity newAdvertisementEntity) {
    Map<String, String> contextData =
        Map.of("adId", newAdvertisementEntity.getAdvertisementId().toString(), "adTitle",
            newAdvertisementEntity.getTitle(), "userName", user.getName(), "userEmail",
            user.getEmail());

    NotificationEvent event = new NotificationEvent("AD_CREATED", user.getUserId(), contextData);

    String routingKey = "notification.event.ad_created";

    publisher.publishEvent(event, routingKey);
  }

  public void publishAdUpdated(UsersEntity editor, AdvertisementEntity updatedAdvertisementEntity) {
    Map<String, String> contextData =
        Map.of("adId", updatedAdvertisementEntity.getAdvertisementId().toString(), "adTitle",
            updatedAdvertisementEntity.getTitle(), "editorName", editor.getName());

    NotificationEvent event = new NotificationEvent("AD_UPDATED", editor.getUserId(), contextData);

    String routingKey = "notification.event.ad_updated";

    publisher.publishEvent(event, routingKey);
  }
}
