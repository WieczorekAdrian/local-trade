package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewRequestDto;
import io.github.adrian.wieczorek.local_trade.service.review.dto.ReviewResponseDto;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewFinder;
import io.github.adrian.wieczorek.local_trade.service.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;
  private final ReviewFinder reviewFinder;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(reviewFinder.getAllMyReviews(userDetails));
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/{tradeId}")
  public ResponseEntity<ReviewResponseDto> postReview(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID tradeId,
      @RequestBody @Valid ReviewRequestDto reviewRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(reviewService.postReview(userDetails, tradeId, reviewRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal UserDetails userDetails,
      @PathVariable UUID reviewId) {
    reviewService.deleteReviewByAdmin(userDetails, reviewId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
