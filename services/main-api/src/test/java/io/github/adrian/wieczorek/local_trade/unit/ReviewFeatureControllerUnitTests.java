package io.github.adrian.wieczorek.local_trade.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.controller.ReviewController;
import io.github.adrian.wieczorek.local_trade.security.JwtBlacklistService;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewFinder;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableMethodSecurity
@WebMvcTest(controllers = ReviewController.class)
public class ReviewFeatureControllerUnitTests {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private ReviewService reviewService;
  @MockitoBean
  JwtService jwtService;
  @MockitoBean
  JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean
  private ReviewFinder reviewFinder;
  @MockitoBean
  private JwtBlacklistService jwtBlacklistService;

  private ReviewResponseDto reviewResponseDto;
  private List<ReviewResponseDto> reviewResponseDtoList;
  private TradeEntity tradeEntity;

  @BeforeEach
  public void setup() {
    reviewResponseDto = new ReviewResponseDto(5, "good", UUID.randomUUID());
    reviewResponseDtoList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      reviewResponseDtoList.add(reviewResponseDto);
    }

    tradeEntity = new TradeEntity();
    tradeEntity.setTradeId(reviewResponseDto.reviewId());
    UserDetails userDetails = mock(UserDetails.class);

  }

  @Test
  @WithMockUser("test@test.com")
  public void getAllMyReviewsControllerTest_returnsOk() throws Exception {

    when(reviewFinder.getAllMyReviews(any(UserDetails.class))).thenReturn(reviewResponseDtoList);

    mockMvc.perform(get("/reviews").contentType(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isOk()).andExpect(jsonPath("$[0].rating").value(5))
        .andExpect(jsonPath("$[0].comment").value("good"))
        .andExpect(jsonPath("$[0].reviewId").value(reviewResponseDto.reviewId().toString()));

  }

  @Test
  public void getAllMyReviewsControllerTestUserNotLoggedIn_returnsUnauthorized() throws Exception {

    mockMvc.perform(get("/reviews").with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser("test@test.com")
  public void getAllMyReviewsControllerTestButReturnsNothing() throws Exception {
    when(reviewFinder.getAllMyReviews(any(UserDetails.class))).thenReturn(List.of());

    mockMvc.perform(get("/reviews")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser("test@test.com")
  public void postReview_thenReviewIsPosted() throws Exception {
    ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "good");

    when(reviewService.postReview(any(UserDetails.class), eq(tradeEntity.getTradeId()),
        any(ReviewRequestDto.class))).thenReturn(reviewResponseDto);

    String reviewJson = objectMapper.writeValueAsString(reviewRequestDto);

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewJson).with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser("test@test.com")
  public void postReviewWithBadData_thenReturnsBadRequest() throws Exception {
    var reviewRequestDto = new ReviewRequestDto(99, "good");

    String reviewJson = objectMapper.writeValueAsString(reviewRequestDto);
    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).content(reviewJson).with(csrf()))
        .andExpect(status().isBadRequest());

  }

  @Test
  public void postReviewWithBadUser_thenReturnsUnauthorized() throws Exception {

    mockMvc
        .perform(post("/reviews/" + tradeEntity.getTradeId())
            .contentType(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isUnauthorized());

  }

  @Test
  @WithMockUser(value = "test@test.com", roles = "ADMIN")
  public void deleteReview_thenReviewIsDeleted() throws Exception {
    var review = new ReviewEntity();
    review.setReviewId(reviewResponseDto.reviewId());

    doNothing().when(reviewService).deleteReviewByAdmin(any(UserDetails.class),
        eq(review.getReviewId()));

    mockMvc.perform(delete("/reviews/" + review.getReviewId()).with(csrf()))
        .andExpect(status().isNoContent());

  }

  @Test
  @WithMockUser(value = "test@test.com", roles = "USER")
  public void deleteReviewWithRoleUser_thenReviewIsNotDeleted_throwsForbidden() throws Exception {
    var review = new ReviewEntity();
    review.setReviewId(reviewResponseDto.reviewId());

    doNothing().when(reviewService).deleteReviewByAdmin(any(UserDetails.class),
        eq(review.getReviewId()));

    mockMvc.perform(delete("/reviews/" + review.getReviewId()).with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  @WithMockUser(value = "test@test.com", roles = "ADMIN")
  public void deleteReviewWithRoleAdminBadRequest_thenReviewIsNotDeleted_throwsBadRequest()
      throws Exception {
    String invalidUUID = "this-is-not-a-uuid";
    mockMvc.perform(delete("/reviews/" + invalidUUID).with(csrf()))
        .andExpect(status().isBadRequest());

  }

}
