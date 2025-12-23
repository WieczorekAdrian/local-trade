package io.github.adrian.wieczorek.local_trade.service.image;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisementEntity_id", nullable = false)
    private AdvertisementEntity advertisementEntity;
    @Column(name = "\"key\"", length = 1024)
    private String key;
    @Column(length = 1024)
    private String url;
    @Column(name = "thumbnail_key", length = 1024)
    private String thumbnailKey;
    @Column(length = 1024)
    private String thumbnailUrl;

    private Integer sortOrder;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String contentType;

    private Long size;

    @Column(name = "image_id")
    private UUID imageId = UUID.randomUUID();
}
