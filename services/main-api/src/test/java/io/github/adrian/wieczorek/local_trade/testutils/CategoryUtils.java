package io.github.adrian.wieczorek.local_trade.testutils;

import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;

import java.util.UUID;

public class CategoryUtils {
  public static CategoryEntity createCategory() {
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setId(1);
    categoryEntity.setName("test");
    categoryEntity.setDescription("test");
    categoryEntity.setParentCategory("test");
    categoryEntity.setCategoryId(UUID.randomUUID());
    return categoryEntity;
  }

  public static CategoryEntity createCategoryForIntegrationTests() {
    CategoryEntity categoryEntity = new CategoryEntity();
    categoryEntity.setName("test");
    categoryEntity.setDescription("test");
    categoryEntity.setParentCategory("test");
    categoryEntity.setCategoryId(UUID.randomUUID());
    return categoryEntity;
  }

  public static CategoryDto createCategoryDto() {
    return new CategoryDto(null, "Test category", "Category for testing", "Test parent category");

  }
}
