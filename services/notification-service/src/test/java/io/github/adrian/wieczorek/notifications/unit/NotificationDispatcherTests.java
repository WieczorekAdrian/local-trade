package io.github.adrian.wieczorek.notifications.unit;

import io.github.adrian.wieczorek.dtos.NotificationEvent;

import io.github.adrian.wieczorek.notifications.handlers.AdCreatedHandler;
import io.github.adrian.wieczorek.notifications.handlers.UserRegisteredHandler;
import io.github.adrian.wieczorek.notifications.service.business.NotificationDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationDispatcherTests {

  @Mock
  private AdCreatedHandler adCreatedHandler;
  @Mock
  private UserRegisteredHandler userRegisteredHandler;
  @InjectMocks
  private NotificationDispatcher dispatcher;

  private final UUID id = UUID.randomUUID();
  private Map<String, String> context;

  @BeforeEach
  public void setUp() {
    dispatcher = new NotificationDispatcher(List.of(adCreatedHandler, userRegisteredHandler));

    context = Map.of("title", "Car with rims", "location", "Chicago", "price", "999");

  }

  @Test
  public void whenEventAdCreatedIsCalledAdCreated_thenAdCreatedHandlerIsCalled() {
    NotificationEvent notificationEvent = new NotificationEvent("AD_CREATED", id, context);
    when(adCreatedHandler.supports("AD_CREATED")).thenReturn(true);
    dispatcher.dispatch(notificationEvent);
    verify(adCreatedHandler, times(1)).handle(notificationEvent);
    verify(userRegisteredHandler, never()).handle(notificationEvent);
  }

  @Test
  public void whenEventAdCreatedIsCalledUserCreated_thenAdCreatedHandlerIsCalled() {
    NotificationEvent notificationEvent = new NotificationEvent("USER_REGISTERED", id, context);
    when(userRegisteredHandler.supports("USER_REGISTERED")).thenReturn(true);
    dispatcher.dispatch(notificationEvent);
    verify(adCreatedHandler, never()).handle(notificationEvent);
    verify(userRegisteredHandler, times(1)).handle(notificationEvent);
  }

  @Test
  public void whenEventIsCreated_thenHandlerDoNotExist_returnNothing() {
    NotificationEvent notificationEvent = new NotificationEvent("UNKNOWN_EVENT", id, context);
    dispatcher.dispatch(notificationEvent);
    verify(userRegisteredHandler, never()).handle(notificationEvent);
    verify(adCreatedHandler, never()).handle(notificationEvent);

  }
}
