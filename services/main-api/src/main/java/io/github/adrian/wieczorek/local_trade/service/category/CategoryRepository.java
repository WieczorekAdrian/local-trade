package io.github.adrian.wieczorek.local_trade.service.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
  Optional<Object> findByName(String name);
}
