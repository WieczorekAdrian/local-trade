package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementFilterDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementFilterServiceImpl;
import io.github.adrian.wieczorek.local_trade.testutils.AdFiltersUtils;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvertisementFilterUnitTests {
    @Mock
    private AdvertisementRepository advertisementRepository;
    @InjectMocks
    private AdvertisementFilterServiceImpl advertisementFilterService;
    @Mock
    private AdvertisementDtoMapper advertisementDtoMapper;

    @Test
    public void filterAndPageAdvertisements_thenReturnPageOfAdvertisements() {
        AdvertisementFilterDto advertisementFilterDto = AdFiltersUtils.getAdvertisementFilterDto();
        AdvertisementEntity ad1 = AdUtils.createAdvertisement();
        AdvertisementEntity ad2 = AdUtils.createAdvertisement();
        AdvertisementEntity ad3 = AdUtils.createAdvertisement();
        List<AdvertisementEntity> advertisementEntities = List.of(ad1, ad2, ad3);
        Pageable pageable = PageRequest.of(0, 10);
        when(advertisementRepository.findAll(
                ArgumentMatchers.<Specification<AdvertisementEntity>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(advertisementEntities));
        ResponseAdvertisementDto mockResponseDto = new ResponseAdvertisementDto(LocalDateTime.now(),
                UUID.randomUUID(),UUID.randomUUID(),"testemail@email.pl", 1, BigDecimal.TEN, "Test Title", "img.jpg",
                "Desc", true, "Location", List.of("url"), List.of("thumb"));

        when(advertisementDtoMapper.toResponseAdvertisementDto(any(AdvertisementEntity.class)))
                .thenReturn(mockResponseDto);

        Page<ResponseAdvertisementDto> result = advertisementFilterService
                .filterAndPageAdvertisements(advertisementFilterDto, pageable);

        assertEquals(3, result.getNumberOfElements());
        assertEquals(1, result.getTotalPages());
        // Sprawdzamy, że wszystkie DTO mają aktywność zgodną z filtrem
        assertTrue(result.getContent().stream().allMatch(ResponseAdvertisementDto::active));
    }
    @Test
    public void filterByCategoryIdAndPageAdvertisements_thenReturnPageOfAdvertisements() {
        CategoryEntity categoryEntity = CategoryUtils.createCategory();
        AdvertisementFilterDto advertisementFilterDto = AdFiltersUtils.filterByCategory(categoryEntity.getId());
        List<AdvertisementEntity> advertisementEntities = IntStream.range(0, 10)
                .mapToObj(i->AdUtils.createAdvertisement())
                .toList();
        Pageable pageable = PageRequest.of(0, 10);

        when(advertisementRepository.findAll(ArgumentMatchers.any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(advertisementEntities));

        ResponseAdvertisementDto mockResponseDto = new ResponseAdvertisementDto(LocalDateTime.now(),
                UUID.randomUUID(),UUID.randomUUID(),"testemail@email.pl", categoryEntity.getId(), BigDecimal.TEN, "Test Title", "img.jpg",
                "Desc", true, "Location", List.of("url"), List.of("thumb"));

        when(advertisementDtoMapper.toResponseAdvertisementDto(any(AdvertisementEntity.class)))
                .thenReturn(mockResponseDto);

        Page<ResponseAdvertisementDto> result = advertisementFilterService.filterAndPageAdvertisements(advertisementFilterDto, pageable);

        assertEquals(10, result.getNumberOfElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.getContent().stream().allMatch(ResponseAdvertisementDto::active));

    }
   @Test
   public void filterByCategoryAndPageWithWrongDataAdvertisements_thenReturnNoAdvertisements() {
        int categoryId = 999;
        AdvertisementFilterDto advertisementFilterDto = AdFiltersUtils.filterByCategory(categoryId);
        List<AdvertisementEntity> ad = IntStream.range(0,10)
                .mapToObj(i->AdUtils.createAdvertisement())
                .toList();
        Pageable pageable = PageRequest.of(0, 10);
        when(advertisementRepository.findAll(ArgumentMatchers.any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        Page<ResponseAdvertisementDto> result = advertisementFilterService.filterAndPageAdvertisements(advertisementFilterDto, pageable);

        assertEquals(0, result.getNumberOfElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.getContent().stream().allMatch(ResponseAdvertisementDto::active));
    }
}
