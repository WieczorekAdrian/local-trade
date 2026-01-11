package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.category.service.CategoryFinder;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryFinderUnitTests {

  @InjectMocks
  private CategoryFinder categoryFinder;
  @Mock
  private CategoryRepository categoryRepository;

  @Test
  public void postCategoryId_thenReturnCategoryNameForEndPoints() {
    CategoryEntity categoryEntity = CategoryUtils.createCategory();
    when(categoryRepository.findById(categoryEntity.getId()))
        .thenReturn(Optional.of(categoryEntity));
    String categoryName = categoryFinder.getCategoryNameForEndPoints(categoryEntity.getId());
    Assertions.assertEquals(categoryName, categoryEntity.getName());
  }

  @Test
  public void postNonExistingCategoryId_thenReturnException() {
    Integer nonExistingCategoryId = 9999;
    when(categoryRepository.findById(nonExistingCategoryId)).thenReturn(Optional.empty());
    Assertions.assertThrows(EntityNotFoundException.class, () -> {
      categoryFinder.getCategoryNameForEndPoints(nonExistingCategoryId);
    });
  }
}
