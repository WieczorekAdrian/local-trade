package io.github.adrian.wieczorek.local_trade.service.category.service;

import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryListDto;
import io.github.adrian.wieczorek.local_trade.service.category.mapper.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryFinder {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public String getCategoryNameForEndPoints(Integer categoryId) {
    return categoryRepository.findById(categoryId).map(CategoryEntity::getName)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @Cacheable("categories")
  public CategoryListDto getAllCategories() {
    List<CategoryDto> categoryList =
        categoryRepository.findAll().stream().map(categoryMapper::postCategoryToDto).toList();
    return new CategoryListDto(categoryList);

  }
}
