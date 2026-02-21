package io.github.adrian.wieczorek.local_trade.service.category;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class CategoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer Id;
  @NotBlank(message = "Category name can't be null")
  @Size(min = 3, message = "Min 3 keys")
  private String name;
  @NotBlank(message = "Description can't be blank")
  private String description;
  private String parentCategory;

  private UUID categoryId = UUID.randomUUID();

}
