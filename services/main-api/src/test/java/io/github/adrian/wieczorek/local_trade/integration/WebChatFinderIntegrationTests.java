package io.github.adrian.wieczorek.local_trade.integration;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
@Transactional
public class WebChatFinderIntegrationTests extends AbstractIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private AdvertisementRepository advertisementRepository;
  @Autowired
  private ChatMessageRepository chatMessageRepository;

  @BeforeEach
  public void setup() {
    UsersEntity sender = UserUtils.createUserRoleUser();
    UsersEntity recipient = UserUtils.createUserRoleUser();
    sender.setName("Tomek");
    sender.setEmail("Tomek@wp.pl");
    recipient.setName("Adrian");
    recipient.setEmail("Adrian@wp.pl");
    usersRepository.save(sender);
    usersRepository.save(recipient);
    CategoryEntity category = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(category);
    AdvertisementEntity advertisement =
        AdUtils.createAdvertisementRoleUserForIntegrationTests(category, recipient);
    advertisementRepository.save(advertisement);
    ChatMessageEntity chatMessageEntity = ChatMessageEntity.builder().recipient(recipient)
        .sender(sender).advertisement(advertisement).isRead(false).build();
    chatMessageRepository.save(chatMessageEntity);
  }

  @Test()
  @WithMockUser("Tomek@wp.pl")
  public void getAllMessagesForInbox_thenReturnOk() throws Exception {
    mockMvc
        .perform(
            get("/chats/inbox").contentType(org.springframework.http.MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].partnerEmail").value("Adrian@wp.pl"))
        .andExpect(jsonPath("$[0].unreadCount").value(0));
  }

  @Test
  @WithMockUser("Adrian@wp.pl")
  public void getAllMessagesForInbox_whenAsRecipient_thenReturnUnreadCountOne() throws Exception {
    mockMvc
        .perform(
            get("/chats/inbox").contentType(org.springframework.http.MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].partnerEmail").value("Tomek@wp.pl"))
        .andExpect(jsonPath("$[0].partnerName").value("Tomek"))
        .andExpect(jsonPath("$[0].unreadCount").value(1));
  }

  @Test
  @WithMockUser("Adrian@wp.pl")
  public void markMessagesAsRead_shouldZeroUnreadCount() throws Exception {
    mockMvc.perform(get("/chats/inbox")).andExpect(jsonPath("$[0].unreadCount").value(1));

    mockMvc.perform(patch("/chats/read-all/Tomek@wp.pl")).andExpect(status().isOk());

    mockMvc.perform(get("/chats/inbox")).andExpect(status().isOk())
        .andExpect(jsonPath("$[0].unreadCount").value(0));
  }

  @Test
  public void getInbox_whenAnonymous_shouldReturnForbidden() throws Exception {
    mockMvc.perform(get("/chats/inbox")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser("Tomek@wp.pl")
  public void getHistory_withNonExistentUser_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/chats/history/duch@wp.pl")).andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("User with email duch@wp.pl not found"));
  }

  @Test
  @WithMockUser("Tomek@wp.pl")
  public void getInbox_withPostMethod_shouldReturnMethodNotAllowed() throws Exception {
    mockMvc.perform(post("/chats/inbox")).andExpect(status().isMethodNotAllowed());
  }
}
