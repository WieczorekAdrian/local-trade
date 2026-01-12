package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementFavoriteFinder;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.FavoriteAdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteAdvertisementController {

  private final FavoriteAdvertisementService favoriteAdvertisementService;
  private final AdvertisementFavoriteFinder advertisementFavoriteFinder;

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<Set<FavoriteAdvertisementDto>> getMyFavoriteAdvertisements(
      @AuthenticationPrincipal UserDetails userDetails) {
    String email = userDetails.getUsername();
    return ResponseEntity.ok(advertisementFavoriteFinder.getFavoriteAdvertisements(email));
  }

  @PostMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> addToMyFavoriteAdvertisements(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable @NonNull UUID id) {
    String email = userDetails.getUsername();
    favoriteAdvertisementService.addFavoriteAdvertisement(email, id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> deleteFromMyFavoriteAdvertisements(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable @NonNull UUID id) {
    String email = userDetails.getUsername();
    favoriteAdvertisementService.deleteFavoriteAdvertisement(email, id);
    return ResponseEntity.noContent().build();

  }
}
