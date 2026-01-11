package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewRepository;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.service.review.mapper.ReviewResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewFinder;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewFinderUnitTests {
  @InjectMocks
  private ReviewFinder reviewFinder;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private UsersService usersService;
  @Mock
  ReviewResponseDtoMapper reviewResponseDtoMapper;

  private UserDetails userDetails;
  private ReviewEntity reviewEntity;
  private UsersEntity reviewedUser;
  private ReviewResponseDto reviewResponseDto;

  @BeforeEach
  void setUp() {
    TradeEntity tradeEntity = new TradeEntity();
    userDetails = mock(UserDetails.class);
    UsersEntity reviewer = UserUtils.createUserRoleUser();
    reviewedUser = UserUtils.createUserRoleUser();
    UUID reviewId = UUID.randomUUID();

    reviewEntity = new ReviewEntity(1L, tradeEntity, reviewer, reviewedUser, reviewId, 5, "good");

    reviewResponseDto =
        new ReviewResponseDto(reviewEntity.getRating(), reviewEntity.getComment(), reviewId);
  }

  @Test
  public void getAllMyReviews_thenReturnsAllReviews() {

    List<ReviewEntity> reviewEntities = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      reviewEntities.add(reviewEntity);
    }

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(reviewRepository.findAllByReviewedUserOrReviewer(reviewedUser, reviewedUser))
        .thenReturn(reviewEntities);
    when(reviewResponseDtoMapper.toDto(any(ReviewEntity.class))).thenReturn(reviewResponseDto);

    var result = reviewFinder.getAllMyReviews(userDetails);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(5, result.size());
    Assertions.assertEquals(reviewEntity.getComment(), result.get(0).comment());
  }

  @Test
  public void getAllMyReviews_NoUserFound_throwsUserNotFoundException() {
    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername()))
        .thenThrow(UserNotFoundException.class);
    Assertions.assertThrows(UserNotFoundException.class,
        () -> reviewFinder.getAllMyReviews(userDetails));
    verify(reviewRepository, never()).findAllByReviewedUserOrReviewer(any(), any());
    verify(reviewResponseDtoMapper, never()).toDto(any());
  }

}
