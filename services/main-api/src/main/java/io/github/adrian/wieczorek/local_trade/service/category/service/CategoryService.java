package io.github.adrian.wieczorek.local_trade.service.category.service;

import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryService {

  @Transactional
  CategoryDto postCategory(CategoryDto category);

  @Transactional
  CategoryDto changeCategory(Integer id, CategoryDto categoryDto);

  @Transactional
  void deleteCategory(Integer categoryId);

  @Transactional
  CategoryEntity getCategoryEntityById(Integer id);
}
