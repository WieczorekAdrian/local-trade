package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.*;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class ReviewEntityIntegrationTests extends AbstractIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private AdUtilsIntegrationTests adUtilsIntegrationTests;
  @Autowired
  UsersRepository usersRepository;

  private UsersEntity seller;
  private UsersEntity buyer;
  private ReviewEntity reviewEntity;
  private TradeEntity tradeEntity;

  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private AdvertisementRepository advertisementRepository;
  @Autowired
  private TradeRepository tradeRepository;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    buyer = UserUtils.createUserRoleUserBuyer();
    seller = UserUtils.createUserRoleUserSeller();
    usersRepository.save(buyer);
    usersRepository.save(seller);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);
    AdvertisementEntity advertisementEntity =
        adUtilsIntegrationTests.createIntegrationAd(seller, categoryEntity);
    advertisementRepository.save(advertisementEntity);
    tradeEntity = TradeUtils.createTestTrade(seller, buyer, advertisementEntity);
    tradeRepository.save(tradeEntity);
  }

  @AfterEach
  public void cleanup() {
    reviewRepository.deleteAll();
    tradeRepository.deleteAll();
    advertisementRepository.deleteAll();
    categoryRepository.deleteAll();
    usersRepository.deleteAll();
  }

  @Test
  @WithMockUser(value = "buyer@test.com", roles = "USER")
  public void getAllReviews() throws Exception {
    reviewEntity = ReviewUtils.createTestReview(tradeEntity, buyer, seller);
    reviewRepository.save(reviewEntity);
    List<ReviewEntity> reviewEntityList = reviewRepository.findAll();
    mockMvc.perform(get("/reviews").with(csrf())).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(reviewEntityList.size()))
        .andExpect(jsonPath("$[0].rating").value(reviewEntityList.get(0).getRating()))
        .andExpect(jsonPath("$[0].comment").value(reviewEntityList.get(0).getComment())).andExpect(
            jsonPath("$[0].reviewId").value(reviewEntityList.get(0).getReviewId().toString()));

  }

  @Test
  public void getReviewsUserNotAuthorized_throwsForbidden() throws Exception {
    mockMvc.perform(get("/reviews").with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(value = "stranger@test.com", roles = "USER")
  public void getAllReviewsWithEmptyList_thenReturnOk() throws Exception {
    UsersEntity stranger = UserUtils.createUserRoleUser();
    stranger.setEmail("stranger@test.com");
    usersRepository.save(stranger);

    mockMvc.perform(get("/reviews").with(csrf())).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

  }

  @Test
  @WithMockUser(value = "buyer@test.com", roles = "USER")
  public void getAllReviews_whenOtherUsersHaveReviews_returnsOnlyOwnReviews() throws Exception {
    reviewEntity = ReviewUtils.createTestReview(tradeEntity, buyer, seller);
    reviewRepository.save(reviewEntity);

    UsersEntity buyer2 = UserUtils.createUserRoleUser();
    buyer2.setEmail("buyer2@test.com");
    usersRepository.save(buyer2);

    ReviewEntity reviewEntity2 = ReviewUtils.createTestReview(tradeEntity, buyer2, seller);
    reviewEntity2.setReviewId(UUID.randomUUID());
    reviewRepository.save(reviewEntity2);

    mockMvc.perform(get("/reviews").with(csrf())).andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].reviewId").value(reviewEntity.getReviewId().toString()));
  }

  @Test
  @WithMockUser(value = "buyer@test.com")
  public void postReview_thenReviewIsPosted_returnsOk() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(1, "Everything good");
    tradeEntity.setStatus(TradeStatus.COMPLETED);
    tradeRepository.save(tradeEntity);

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rating").value(reviewRequestDto.rating()))
        .andExpect(jsonPath("$.comment").value(reviewRequestDto.comment()));

  }

  @Test
  @WithMockUser(value = "buyer@test.com")
  public void postReview_thenTradeIsNotCompleted_returnsConflict() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(1, "Everything good");

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  public void postReview_thenUserIsNotLoggedIn_throwsForbidden() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(1, "Everything good");

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  @WithMockUser(value = "testadmin@test.com", roles = "ADMIN")
  public void deleteReview_thenReviewIsDeleted_returnsOk() throws Exception {
    reviewEntity = ReviewUtils.createTestReview(tradeEntity, buyer, seller);
    reviewRepository.save(reviewEntity);
    UsersEntity admin = UserUtils.createUserRoleAdmin();
    usersRepository.save(admin);

    mockMvc.perform(delete("/reviews/" + reviewEntity.getReviewId().toString()).with(csrf()))
        .andExpect(status().isNoContent());

    Assertions.assertEquals(Optional.empty(),
        reviewRepository.findByReviewId(reviewEntity.getReviewId()));
  }

  @Test
  @WithMockUser(value = "test@test.com", roles = "USER")
  public void deleteReview_thenUserIsNotAdmin_thenReturnsForbidden() throws Exception {
    reviewEntity = ReviewUtils.createTestReview(tradeEntity, buyer, seller);
    reviewRepository.save(reviewEntity);
    UsersEntity admin = UserUtils.createUserRoleUser();
    usersRepository.save(admin);

    mockMvc.perform(delete("/reviews/" + reviewEntity.getReviewId().toString()).with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  @WithMockUser(value = "testadmin@test.com", roles = "ADMIN")
  public void deleteReviewWithBadUUID_returnsBadRequest() throws Exception {
    UsersEntity admin = UserUtils.createUserRoleAdmin();
    usersRepository.save(admin);

    mockMvc.perform(delete("/reviews/" + "1233123123123").with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(value = "buyer@test.com", roles = "USER")
  public void postReview_thenRatingIsNotInRange_returnsBadRequest() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(99, "Everything good");
    tradeEntity.setStatus(TradeStatus.COMPLETED);
    tradeRepository.save(tradeEntity);

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(value = "buyer@test.com", roles = "USER")
  public void postReview_thenReviewIsAlreadyPosted_returnsConflict() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(3, "Everything good");
    reviewEntity = ReviewUtils.createTestReview(tradeEntity, buyer, seller);
    reviewRepository.save(reviewEntity);
    tradeEntity.setStatus(TradeStatus.COMPLETED);
    tradeRepository.save(tradeEntity);

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(value = "stranger@test.com")
  public void postReview_thenLoggedInUserIsNotAPartOfTrade_returns() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(1, "Everything good");
    var stranger = UserUtils.createUserRoleUser();
    stranger.setEmail("stranger@test.com");
    usersRepository.save(stranger);

    tradeEntity.setStatus(TradeStatus.COMPLETED);
    tradeRepository.save(tradeEntity);

    var reviewRequestDtToString = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewRequestDtToString).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
