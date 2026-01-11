package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementFilterDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementFilterService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/advertisements")
@RequiredArgsConstructor
public class AdvertisementFilterController {

  private final AdvertisementFilterService advertisementFilterService;

  @GetMapping("/search")
  @Operation(summary = "Filter advertisements",
      description = "Sortowanie po polu \", allowableValues = {\"PRICE\",\"TITLE\",\"CREATED_AT} , Kierunek Sortowania SortDirection allowableValues = ASC,DESC")
  public ResponseEntity<Page<ResponseAdvertisementDto>> filterAndPageAdvertisements(
      @RequestParam(required = false) Integer categoryId,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false, name = "title") String titleFilter,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) Boolean active,
      @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
      @PageableDefault(size = 20) Pageable pageable) {
    AdvertisementFilterDto filterDto =
        new AdvertisementFilterDto(categoryId, minPrice, maxPrice, location, titleFilter, active);
    return ResponseEntity
        .ok(advertisementFilterService.filterAndPageAdvertisements(filterDto, pageable));
  }
}
