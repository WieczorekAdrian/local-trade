package io.github.adrian.wieczorek.local_trade.service.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDto(Integer id,
		@NotBlank(message = "Name can't be blank") @Size(min = 3, message = "Name must be 3 characters long") String name,
		@NotBlank(message = "Description can't be blank") String description, String parentCategory) {

}
