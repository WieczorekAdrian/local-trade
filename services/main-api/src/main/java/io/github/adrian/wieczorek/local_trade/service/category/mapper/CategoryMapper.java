package io.github.adrian.wieczorek.local_trade.service.category.mapper;

import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  CategoryDto postCategoryToDto(CategoryEntity categoryEntity);

  CategoryEntity postCategoryFromDto(CategoryDto categoryDto);

}
