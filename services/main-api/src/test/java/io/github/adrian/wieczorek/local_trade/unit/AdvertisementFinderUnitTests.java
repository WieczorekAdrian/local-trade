package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.SimpleAdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementFinder;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvertisementFinderUnitTests {
  @InjectMocks
  private AdvertisementFinder advertisementFinder;
  @Mock
  private AdvertisementRepository advertisementRepository;
  @Mock
  private AdvertisementDtoMapper advertisementDtoMapper;
  @Mock
  private CategoryRepository categoryRepository;
  @Mock
  private SimpleAdvertisementDtoMapper simpleAdvertisementDtoMapper;

  @Test
  void getAdvertisementById_thenAdvertisementIsNotFound() {
    UUID advertisementId = UUID.randomUUID();
    when(advertisementRepository.findByAdvertisementId(advertisementId))
        .thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class,
        () -> advertisementFinder.getAdvertisementById(advertisementId));
  }

  @Test
  void getAdvertisementById_thenAdvertisementIsReturned() {
    AdvertisementEntity advertisementEntity = AdUtils.createAdvertisement();
    var category = CategoryUtils.createCategory();
    advertisementEntity.setCategoryEntity(category);
    advertisementEntity.setAdvertisementId(UUID.randomUUID());

    var mockResponseDto = new ResponseAdvertisementDto(LocalDateTime.now(),
        advertisementEntity.getAdvertisementId(), UUID.randomUUID(), "randomemail@email.com",
        advertisementEntity.getCategoryEntity().getId(), advertisementEntity.getPrice(),
        advertisementEntity.getTitle(), advertisementEntity.getImage(),
        advertisementEntity.getDescription(), advertisementEntity.isActive(),
        advertisementEntity.getLocation(), new ArrayList<>(), new ArrayList<>());

    when(advertisementDtoMapper.toResponseAdvertisementDto(advertisementEntity))
        .thenReturn(mockResponseDto);
    when(advertisementRepository.findByAdvertisementId(advertisementEntity.getAdvertisementId()))
        .thenReturn(Optional.of(advertisementEntity));

    var responseAdvertisementDto =
        advertisementFinder.getAdvertisementById(advertisementEntity.getAdvertisementId());

    assertNotNull(responseAdvertisementDto);
    assertEquals(advertisementEntity.getAdvertisementId(),
        responseAdvertisementDto.advertisementId());
    assertEquals(advertisementEntity.getPrice(), responseAdvertisementDto.price());
  }

  @Test
  public void findAdvertisementsByCategoryId_thenReturnAllAdvertisements() {
    CategoryEntity categoryEntity = CategoryUtils.createCategory();
    AdvertisementEntity advertisementEntity = AdUtils.createAdvertisement();
    AdvertisementEntity advertisementEntity2 = AdUtils.createAdvertisement();
    advertisementEntity2.setId(2);

    List<AdvertisementEntity> advertisementEntities =
        List.of(advertisementEntity, advertisementEntity2);

    SimpleAdvertisementResponseDto dto1 = new SimpleAdvertisementResponseDto(
        advertisementEntity.getAdvertisementId(), advertisementEntity.getTitle());
    SimpleAdvertisementResponseDto dto2 = new SimpleAdvertisementResponseDto(
        advertisementEntity2.getAdvertisementId(), advertisementEntity2.getTitle());

    when(advertisementRepository.findByCategoryEntityId(categoryEntity.getId()))
        .thenReturn(advertisementEntities);

    when(simpleAdvertisementDtoMapper.advertisementToSimpleDto(advertisementEntity))
        .thenReturn(dto1);
    when(simpleAdvertisementDtoMapper.advertisementToSimpleDto(advertisementEntity2))
        .thenReturn(dto2);

    List<SimpleAdvertisementResponseDto> resultDtos =
        advertisementFinder.findAllAdvertisementsByCategoryId(categoryEntity.getId());

    assertNotNull(resultDtos);
    assertEquals(2, resultDtos.size());
    assertEquals(dto1, resultDtos.get(0));
    assertEquals(dto2, resultDtos.get(1));
    verify(advertisementRepository, times(1)).findByCategoryEntityId(categoryEntity.getId());

    verify(simpleAdvertisementDtoMapper, times(2))
        .advertisementToSimpleDto(any(AdvertisementEntity.class));
    verify(simpleAdvertisementDtoMapper, times(1)).advertisementToSimpleDto(advertisementEntity);
    verify(simpleAdvertisementDtoMapper, times(1)).advertisementToSimpleDto(advertisementEntity2);
  }

  @Test
  public void findNonExistingAdvertisementsByCategoryId_shouldReturnEmptyList() {
    Integer nonExistingCategoryId = 9999;

    when(advertisementRepository.findByCategoryEntityId(nonExistingCategoryId))
        .thenReturn(Collections.emptyList());

    List<SimpleAdvertisementResponseDto> resultDtos =
        advertisementFinder.findAllAdvertisementsByCategoryId(nonExistingCategoryId);

    assertNotNull(resultDtos);
    assertTrue(resultDtos.isEmpty());

    verify(advertisementRepository, times(1)).findByCategoryEntityId(nonExistingCategoryId);
    verifyNoInteractions(simpleAdvertisementDtoMapper);
  }

  @Test
  public void emptyAdvertisementList_thenReturnEmptyList() {
    CategoryEntity categoryEntity = CategoryUtils.createCategory();
    List<AdvertisementEntity> advertisementEntity = List.of();

    when(advertisementRepository.findByCategoryEntityId(categoryEntity.getId()))
        .thenReturn(advertisementEntity);

    List<SimpleAdvertisementResponseDto> advertisementEntities =
        advertisementFinder.findAllAdvertisementsByCategoryId(categoryEntity.getId());

    assertNotNull(advertisementEntities);
    assertEquals(0, advertisementEntities.size());
    verify(advertisementRepository, times(1)).findByCategoryEntityId(categoryEntity.getId());
  }
}
