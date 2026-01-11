package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.exceptions.GlobalConflictException;
import io.github.adrian.wieczorek.local_trade.exceptions.TradeAccessDenied;
import io.github.adrian.wieczorek.local_trade.exceptions.TradeReviewedGlobalConflictException;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.review.mapper.ReviewResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeService;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.review.ReviewRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewServiceImpl;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ReviewServiceImplUnitTests {

  @InjectMocks
  private ReviewServiceImpl reviewService;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private TradeService tradeService;
  @Mock
  UsersService usersService;
  @Mock
  ReviewResponseDtoMapper reviewResponseDtoMapper;
  @Mock
  TradeRepository tradeRepository;
  @Mock
  UsersRepository usersRepository;

  private UserDetails userDetails;
  private ReviewEntity reviewEntity;
  private TradeEntity tradeEntity;
  private UsersEntity reviewer;
  private UsersEntity reviewedUser;
  private ReviewResponseDto reviewResponseDto;

  @BeforeEach
  void setUp() {
    tradeEntity = new TradeEntity();
    userDetails = mock(UserDetails.class);
    reviewer = UserUtils.createUserRoleUser();
    reviewedUser = UserUtils.createUserRoleUser();
    UUID reviewId = UUID.randomUUID();

    reviewEntity = new ReviewEntity(1L, tradeEntity, reviewer, reviewedUser, reviewId, 5, "good");

    reviewResponseDto =
        new ReviewResponseDto(reviewEntity.getRating(), reviewEntity.getComment(), reviewId);
  }

  @Test
  public void postReview_thenReviewIsPosted_returnsReview() {
    tradeEntity.setSeller(reviewer);
    tradeEntity.setBuyer(reviewedUser);
    tradeEntity.setStatus(TradeStatus.COMPLETED);
    reviewer.setId(2);
    reviewer.setEmail("seller@seller.com");
    reviewedUser.setId(1);
    reviewedUser.setEmail("buyer@buyer.com");

    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(tradeService.getTradeEntityByTradeId(tradeEntity.getTradeId())).thenReturn(tradeEntity);
    when(reviewRepository.existsByTradeEntityAndReviewer(tradeEntity, reviewedUser))
        .thenReturn(Boolean.FALSE);
    when(reviewRepository.findAllByReviewedUser(reviewer)).thenReturn(List.of(reviewEntity));
    when(reviewResponseDtoMapper.toDto(any(ReviewEntity.class))).thenReturn(reviewResponseDto);

    when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(reviewEntity);
    when(tradeService.saveTrade(any(TradeEntity.class))).thenReturn(tradeEntity);
    when(usersService.saveUser(any(UsersEntity.class))).thenReturn(reviewedUser);

    var result = reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto);

    ArgumentCaptor<ReviewEntity> reviewCaptor = ArgumentCaptor.forClass(ReviewEntity.class);
    ArgumentCaptor<TradeEntity> tradeCaptor = ArgumentCaptor.forClass(TradeEntity.class);
    ArgumentCaptor<UsersEntity> userCaptor = ArgumentCaptor.forClass(UsersEntity.class);

    verify(reviewRepository).save(reviewCaptor.capture());
    verify(tradeService).saveTrade(tradeCaptor.capture());
    verify(usersService).saveUser(userCaptor.capture());

    Assertions.assertNotNull(result);
    Assertions.assertEquals(5, result.rating());
    Assertions.assertEquals("good", result.comment());

    ReviewEntity capturedReviewEntity = reviewCaptor.getValue();
    Assertions.assertEquals(reviewedUser, capturedReviewEntity.getReviewer());
    Assertions.assertEquals(reviewer, capturedReviewEntity.getReviewedUser());
    Assertions.assertEquals(tradeEntity, capturedReviewEntity.getTradeEntity());

    TradeEntity capturedTradeEntity = tradeCaptor.getValue();
    Assertions.assertTrue(capturedTradeEntity.isBuyerLeftReview());
    Assertions.assertFalse(capturedTradeEntity.isSellerLeftReview());

    UsersEntity capturedUser = userCaptor.getValue();
    Assertions.assertEquals(reviewer, capturedUser);
    Assertions.assertEquals(1, capturedUser.getRatingCount());
    Assertions.assertEquals(5.0, capturedUser.getAverageRating());

    verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
    verify(tradeService, times(1)).saveTrade(any(TradeEntity.class));
    verify(usersService, times(1)).saveUser(any(UsersEntity.class));
  }

  @Test
  public void postReview_thenUserIsNotFound_throwsUserNotFoundException() {
    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername()))
        .thenThrow(UserNotFoundException.class);

    Assertions.assertThrows(UserNotFoundException.class,
        () -> reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto));

    verify(reviewRepository, never()).save(any(ReviewEntity.class));
    verify(tradeService, never()).saveTrade(any(TradeEntity.class));
    verify(usersService, never()).saveUser(any(UsersEntity.class));
  }

  @Test
  public void postReview_thenTradeIsNotFound_throwsEntityNotFoundException() {
    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(tradeService.getTradeEntityByTradeId(tradeEntity.getTradeId()))
        .thenThrow(EntityNotFoundException.class);

    Assertions.assertThrows(EntityNotFoundException.class,
        () -> reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto));

    verify(reviewRepository, never()).save(any(ReviewEntity.class));
    verify(usersService, never()).saveUser(any(UsersEntity.class));
    verify(tradeService, never()).saveTrade(any(TradeEntity.class));
  }

  @Test
  public void postReview_thenUserIsTheSameAs_throwsEntityNotFoundException() {
    var stranger = UserUtils.createUserRoleUser();
    stranger.setId(999);
    stranger.setEmail("imnotapartofthistrade@gmail.com");
    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());
    tradeEntity.setStatus(TradeStatus.COMPLETED);

    tradeEntity.setSeller(stranger);
    tradeEntity.setBuyer(stranger);

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(tradeService.getTradeEntityByTradeId(tradeEntity.getTradeId())).thenReturn(tradeEntity);

    Assertions.assertThrows(TradeAccessDenied.class,
        () -> reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto));

    verify(reviewRepository, never()).save(any(ReviewEntity.class));
    verify(usersService, never()).saveUser(any(UsersEntity.class));
    verify(tradeService, never()).saveTrade(any(TradeEntity.class));
  }

  @Test
  public void postReview_thenTradeStatusIsNotComplete_throwsSecurityException() {
    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());
    tradeEntity.setSeller(reviewedUser);
    tradeEntity.setStatus(TradeStatus.CANCELLED);

    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(tradeService.getTradeEntityByTradeId(tradeEntity.getTradeId())).thenReturn(tradeEntity);

    Assertions.assertThrows(GlobalConflictException.class,
        () -> reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto));

    verify(reviewRepository, never()).save(any(ReviewEntity.class));
    verify(usersService, never()).saveUser(any(UsersEntity.class));
    verify(tradeService, never()).saveTrade(any(TradeEntity.class));

  }

  @Test
  public void postReview_thenReviewIsAlreadyPosted_throwsSecurityException() {
    var reviewRequestDto =
        new ReviewRequestDto(reviewEntity.getRating(), reviewEntity.getComment());
    tradeEntity.setSeller(reviewedUser);
    tradeEntity.setStatus(TradeStatus.COMPLETED);
    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(tradeService.getTradeEntityByTradeId(tradeEntity.getTradeId())).thenReturn(tradeEntity);
    when(reviewRepository.existsByTradeEntityAndReviewer(tradeEntity, reviewedUser))
        .thenReturn(Boolean.TRUE);

    Assertions.assertThrows(TradeReviewedGlobalConflictException.class,
        () -> reviewService.postReview(userDetails, tradeEntity.getTradeId(), reviewRequestDto));

    verify(reviewRepository, never()).save(any(ReviewEntity.class));
    verify(usersService, never()).saveUser(any(UsersEntity.class));
    verify(tradeService, never()).saveTrade(any(TradeEntity.class));
  }

  @Test
  public void deleteReview_thenReviewIsDeleted() {
    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(reviewRepository.findByReviewId(reviewEntity.getReviewId()))
        .thenReturn(Optional.of(reviewEntity));

    reviewService.deleteReviewByAdmin(userDetails, reviewEntity.getReviewId());
    verify(reviewRepository, times(1)).delete(any(ReviewEntity.class));

  }

  @Test
  public void deleteReviewWithBadUser_thenReviewIsNotDeleted() {
    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername()))
        .thenThrow(UserNotFoundException.class);

    Assertions.assertThrows(UserNotFoundException.class,
        () -> reviewService.deleteReviewByAdmin(userDetails, reviewEntity.getReviewId()));

    verify(reviewRepository, never()).delete(any(ReviewEntity.class));

  }

  @Test
  public void deleteReviewButReviewIsNotPresent_throwsEntityException() {
    when(userDetails.getUsername()).thenReturn(reviewedUser.getUsername());
    when(usersService.getCurrentUser(userDetails.getUsername())).thenReturn(reviewedUser);
    when(reviewRepository.findByReviewId(reviewEntity.getReviewId())).thenReturn(Optional.empty());

    Assertions.assertThrows(EntityNotFoundException.class,
        () -> reviewService.deleteReviewByAdmin(userDetails, reviewEntity.getReviewId()));

    verify(reviewRepository, never()).delete(any(ReviewEntity.class));

  }

}
