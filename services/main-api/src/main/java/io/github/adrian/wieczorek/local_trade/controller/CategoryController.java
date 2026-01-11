package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryListDto;
import io.github.adrian.wieczorek.local_trade.service.category.service.CategoryFinder;
import io.github.adrian.wieczorek.local_trade.service.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryFinder categoryFinder;

  @GetMapping
  @Operation(summary = "Get all categories")
  public ResponseEntity<CategoryListDto> getAllCategories() {
    CategoryListDto categories = categoryFinder.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping()
  public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto category) {
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.postCategory(category));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto,
      @PathVariable Integer id) {
    return ResponseEntity.ok(categoryService.changeCategory(id, categoryDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
