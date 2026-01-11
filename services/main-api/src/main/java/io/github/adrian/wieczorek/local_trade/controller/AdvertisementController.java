package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.facade.NewAdvertisementFacade;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementFinder;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

  private final AdvertisementFinder advertisementFinder;
  private final AdvertisementService advertisementService;
  private final NewAdvertisementFacade newAdvertisementFacade;

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/add")
  public ResponseEntity<SimpleAdvertisementResponseDto> createAdd(
      @RequestBody RequestAdvertisementDto ad, @AuthenticationPrincipal UserDetails userDetails) {
    SimpleAdvertisementResponseDto created = advertisementService.addAd(ad, userDetails);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/get/{id}")
  @Operation(summary = "Get advertisement by advertisement id")
  public ResponseEntity<ResponseAdvertisementDto> getAdd(@PathVariable UUID id) {
    ResponseAdvertisementDto advertisement = advertisementFinder.getAdvertisementById(id);
    return ResponseEntity.ok(advertisement);
  }

  @PutMapping("/update/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update advertisement by advertisement id and user")
  public ResponseEntity<AdvertisementUpdateDto> updateAdd(@PathVariable Integer id,
      @RequestBody AdvertisementUpdateDto ad, @AuthenticationPrincipal UserDetails userDetails) {
    AdvertisementUpdateDto updated = advertisementService.changeAdvertisement(ad, userDetails, id);
    return ResponseEntity.ok(updated);
  }

  @PreAuthorize("hasRole('ADMIN')or @advertisementSecurityServiceImpl.isOwner(authentication,id)")
  @DeleteMapping("/delete/{id}")
  @Operation(summary = "Delete advertisement by advertisement id and user")
  public ResponseEntity<Void> deleteAdd(@PathVariable Integer id,
      @AuthenticationPrincipal UserDetails userDetails) {
    advertisementService.deleteAdvertisement(userDetails, id);
    return ResponseEntity.ok().build();
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping("/new")
  public ResponseEntity<ResponseAdvertisementDto> addWholeAdvertisement(
      @RequestPart RequestAdvertisementDto advertisementDto, @RequestPart List<MultipartFile> files,
      @AuthenticationPrincipal UserDetails userDetails) throws IOException {
    return ResponseEntity
        .ok(newAdvertisementFacade.addWholeAdvertisement(advertisementDto, files, userDetails));
  }
}
