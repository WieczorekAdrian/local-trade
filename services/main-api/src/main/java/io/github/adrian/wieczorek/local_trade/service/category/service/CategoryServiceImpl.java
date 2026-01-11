package io.github.adrian.wieczorek.local_trade.service.category.service;

import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.category.mapper.CategoryMapper;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @CacheEvict(value = "categories", allEntries = true)
  @Transactional
  @Override
  public CategoryDto postCategory(CategoryDto category) {
    CategoryEntity newCategoryEntity = new CategoryEntity();
    newCategoryEntity.setName(category.name());
    newCategoryEntity.setDescription(category.description());
    newCategoryEntity.setParentCategory(category.parentCategory());
    CategoryEntity savedCategoryEntity = categoryRepository.save(newCategoryEntity);
    return categoryMapper.postCategoryToDto(savedCategoryEntity);
  }

  @CacheEvict(value = "categories", allEntries = true)
  @Transactional
  @Override
  public CategoryDto changeCategory(Integer id, CategoryDto categoryDto) {
    CategoryEntity categoryEntity = categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    if (categoryDto.name() != null) {
      categoryEntity.setName(categoryDto.name());
    }
    if (categoryDto.description() != null) {
      categoryEntity.setDescription(categoryDto.description());
    }
    if (categoryDto.parentCategory() != null) {
      categoryEntity.setParentCategory(categoryDto.parentCategory());
    }
    CategoryEntity Saved = categoryRepository.save(categoryEntity);
    return categoryMapper.postCategoryToDto(Saved);
  }

  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  @Override
  public void deleteCategory(Integer categoryId) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new EntityNotFoundException("Category not found");
    }
    categoryRepository.deleteById(categoryId);
  }

  @Transactional
  @Override
  public CategoryEntity getCategoryEntityById(Integer id) {
    log.debug("Getting category with id {}", id);
    return categoryRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
  }

}
