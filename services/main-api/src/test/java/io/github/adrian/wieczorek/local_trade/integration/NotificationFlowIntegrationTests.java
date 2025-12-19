package io.github.adrian.wieczorek.local_trade.integration;

import io.github.adrian.wieczorek.dtos.NotificationEvent;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.facade.AdvertisementEventFacade;
import io.github.adrian.wieczorek.local_trade.service.rabbit.NotificationEventPublisher;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class NotificationFlowIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AdvertisementRepository advertisementRepository;


    private UsersEntity randomUser;
    private AdvertisementEntity randomAdvertisementEntity;

    @Autowired
    private AdvertisementEventFacade advertisementEventFacade;
    @Autowired
    private UsersRepository usersRepository;
    @MockitoBean
    private NotificationEventPublisher notificationEventPublisher;

    @BeforeEach
    public void setup() {
        randomUser = UserUtils.createUserRoleUserUnitTestWithUUID();
        usersRepository.save(randomUser);
        CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
        categoryRepository.save(categoryEntity);
        randomAdvertisementEntity = AdUtils.createAdvertisementRoleUserForIntegrationTests(categoryEntity, randomUser);
        advertisementRepository.save(randomAdvertisementEntity);
    }

    @Test
    @Transactional
    public void happyPath_whenSendingEventToRightHandler_thenReturnOk() {
        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);

        advertisementEventFacade.publishAdCreated(randomUser, randomAdvertisementEntity);

        verify(notificationEventPublisher, times(1))
                .publishEvent(eventCaptor.capture(), routingKeyCaptor.capture());

        NotificationEvent capturedEvent = eventCaptor.getValue();
        assertEquals("AD_CREATED", capturedEvent.getEventType());
        assertEquals(randomUser.getUserId(), capturedEvent.getRecipientUserId());
        assertEquals("notification.event.ad_created", routingKeyCaptor.getValue());
    }
}
