package io.github.adrian.wieczorek.local_trade.service.category.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record CategoryListDto(List<CategoryDto> categories) implements Serializable {
}