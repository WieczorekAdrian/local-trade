package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeStatusRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
public class TradeController {

  private final TradeService tradeService;

  @PreAuthorize("isAuthenticated()")
  @PostMapping()
  public ResponseEntity<TradeResponseDto> tradeInitiation(
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody TradeInitiationRequestDto tradeRequestDto) {
    TradeResponseDto tradeResponseDto = tradeService.tradeInitiation(userDetails, tradeRequestDto);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("{id}")
        .buildAndExpand(tradeResponseDto.id()).toUri();
    return ResponseEntity.created(location).body(tradeResponseDto);
  }

  @PreAuthorize("isAuthenticated()")
  @PatchMapping("/{id}")
  public ResponseEntity<TradeResponseDto> updateTradeStatus(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id,
      @Valid @RequestBody TradeStatusRequestDto tradeRequestDto) {
    return ResponseEntity
        .ok(tradeService.updateTradeStatus(userDetails, id, tradeRequestDto.tradeStatus()));
  }
}
