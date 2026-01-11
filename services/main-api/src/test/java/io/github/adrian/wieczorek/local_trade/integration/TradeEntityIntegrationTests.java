package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeStatusRequestDto;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class TradeEntityIntegrationTests extends AbstractIntegrationTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  UsersRepository usersRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private AdvertisementRepository advertisementRepository;
  @Autowired
  private TradeRepository tradeRepository;
  @Autowired
  private JwtService jwtService;

  private UsersEntity buyer;
  private UsersEntity seller;
  private AdvertisementEntity sellerAdvertisementEntity;
  private CategoryEntity buyerCategoryEntity;

  @BeforeEach
  public void setup() {
    buyerCategoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(buyerCategoryEntity);
    buyer = UserUtils.createUserRoleUser();
    buyer.setEmail("buyer@test.com");
    usersRepository.save(buyer);
    seller = UserUtils.createUserRoleUser();
    seller.setName("seller@test.com");
    usersRepository.save(seller);
    sellerAdvertisementEntity =
        AdUtils.createAdvertisementRoleUserForIntegrationTests(buyerCategoryEntity, buyer);
    sellerAdvertisementEntity.setUser(seller);
    advertisementRepository.save(sellerAdvertisementEntity);
  }

  @ParameterizedTest
  @Transactional
  @CsvSource({"POST, /trades, 201", "PATCH, /trades, 200", "PATCH CANCELLED, /trades, 200"})
  public void tradeInitiation_thenReturnHttpCodes(String httpMethod, String path, int statusCode)
      throws Exception {
    TradeInitiationRequestDto tradeInitiation = new TradeInitiationRequestDto(BigDecimal.valueOf(2),
        sellerAdvertisementEntity.getAdvertisementId());

    var tradeStatusCompleted = new TradeStatusRequestDto(TradeStatus.COMPLETED);
    var tradeStatusCancelled = new TradeStatusRequestDto(TradeStatus.CANCELLED);

    var tradeInitiationToJson = objectMapper.writeValueAsString(tradeInitiation);
    var tradeStatusCompletedToJson = objectMapper.writeValueAsString(tradeStatusCompleted);
    var tradeStatusCancelledToJson = objectMapper.writeValueAsString(tradeStatusCancelled);

    String jwtBuyer = jwtService.generateToken(buyer);

    ResultActions resultActions;

    switch (httpMethod.toUpperCase()) {
      case "POST":
        resultActions = mockMvc
            .perform(post(path).header("Authorization", "Bearer " + jwtBuyer).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(tradeInitiationToJson))
            .andExpect(jsonPath("$.buyerSimpleDto.id").value(buyer.getId()))
            .andExpect(jsonPath("$.sellerSimpleDto.name").value(seller.getName()))
            .andExpect(jsonPath("$.buyerSimpleDto.name").value(buyer.getName()))
            .andExpect(jsonPath("$.sellerSimpleDto.id").value(seller.getId()))
            .andExpect(jsonPath("$.status").value("PROPOSED"))
            .andExpect(jsonPath("$.proposedPrice").value(2))
            .andExpect(jsonPath("$.createdAt").isNotEmpty())
            .andExpect(jsonPath("$.buyerMarkedCompleted").value(false))
            .andExpect(jsonPath("$.sellerMarkedCompleted").value(false));
        break;
      case "PATCH":
        var trade = TradeEntity.builder().advertisementEntity(sellerAdvertisementEntity)
            .buyerLeftReview(false).status(TradeStatus.PROPOSED)
            .createdAt(LocalDateTime.now().minusDays(1)).proposedPrice(BigDecimal.valueOf(2))
            .sellerMarkedCompleted(true).buyerMarkedCompleted(false).buyer(buyer).seller(seller)
            .build();

        tradeRepository.save(trade);
        trade.setCreatedAt(LocalDateTime.now().minusDays(1));
        resultActions = mockMvc
            .perform(patch(path + "/" + trade.getId()).header("Authorization", "Bearer " + jwtBuyer)
                .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(tradeStatusCompletedToJson))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
        break;

      case "PATCH CANCELLED":
        var mockTrade = TradeEntity.builder().advertisementEntity(sellerAdvertisementEntity)
            .buyerLeftReview(false).status(TradeStatus.PROPOSED)
            .createdAt(LocalDateTime.now().minusDays(1)).proposedPrice(BigDecimal.valueOf(2))
            .sellerMarkedCompleted(true).buyerMarkedCompleted(false).buyer(buyer).seller(seller)
            .build();

        tradeRepository.save(mockTrade);
        mockTrade.setCreatedAt(LocalDateTime.now().minusDays(1));
        resultActions = mockMvc
            .perform(patch(path + "/" + mockTrade.getId())
                .header("Authorization", "Bearer " + jwtBuyer).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(tradeStatusCancelledToJson))
            .andExpect(jsonPath("$.status").value("CANCELLED"));
        break;
      default:
        throw new Exception("Invalid HTTP method");
    }
    resultActions.andExpect(status().is(statusCode));

  }

  @Test
  @Transactional
  @WithMockUser("test@test.com")
  public void tradeIsInitiatedWithBadData_thenReturnsBadRequest() throws Exception {
    var tradeInitiationRequest = new TradeInitiationRequestDto(null, null);

    String content = objectMapper.writeValueAsString(tradeInitiationRequest);

    mockMvc
        .perform(
            post("/trades").contentType(MediaType.APPLICATION_JSON).content(content).with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  public void tradeIsInitiatedUserNotLoggedIn_thenReturnsForbidden() throws Exception {

    mockMvc.perform(post("/trades").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  public void tradeCompletedOrCancelledUserNotLogged_thenReturnsForbidden() throws Exception {
    mockMvc.perform(patch("/trades/1").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @Transactional
  @WithMockUser("test@test.com")
  public void tradeCompletedOrCancelledWithBadData_thenReturnsBadRequest() throws Exception {
    var mockTrade =
        TradeEntity.builder().advertisementEntity(sellerAdvertisementEntity).buyerLeftReview(false)
            .status(TradeStatus.PROPOSED).createdAt(LocalDateTime.now().minusDays(1))
            .proposedPrice(BigDecimal.valueOf(2)).sellerMarkedCompleted(true)
            .buyerMarkedCompleted(false).buyer(buyer).seller(seller).build();

    tradeRepository.save(mockTrade);

    var tradeStatusRequestDto = new TradeStatusRequestDto(null);

    var content = objectMapper.writeValueAsString(tradeStatusRequestDto);

    mockMvc.perform(patch("/trades/" + mockTrade.getId()).contentType(MediaType.APPLICATION_JSON)
        .content(content).with(csrf())).andExpect(status().isBadRequest());

  }

}
