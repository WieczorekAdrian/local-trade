package io.github.adrian.wieczorek.local_trade.service.advertisement;

import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvertisementEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToMany(mappedBy = "advertisementEntity", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ImageEntity> imageEntities = new ArrayList<>();

  @Builder.Default
  private UUID advertisementId = UUID.randomUUID();

  @NotBlank
  private String title;

  @NotBlank
  @Size(max = 2000)
  private String description;

  @Size(max = 500)
  private String image;

  private BigDecimal price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private CategoryEntity categoryEntity;

  @NotBlank
  private String location;

  @CreationTimestamp
  private LocalDateTime createdAt;
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "advertisement_favorites", joinColumns = @JoinColumn(name = "advertisement_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  private Set<UsersEntity> favoritedByUsers = new HashSet<>();

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Builder.Default
  private boolean active = true;
  @ManyToOne(fetch = FetchType.LAZY) // wielu ads -> 1 user
  @JoinColumn(name = "user_id", nullable = false)
  private UsersEntity user;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AdvertisementEntity that = (AdvertisementEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : getClass().hashCode();
  }
}
