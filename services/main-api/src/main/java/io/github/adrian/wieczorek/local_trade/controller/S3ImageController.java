package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.image.mapper.ImageMapper;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Finder;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class S3ImageController {

  private final S3Service s3Service;
  private final S3Finder s3Finder;

  @PostMapping("/{id}")
  @Operation(description = "Pass the uuid of advertisement and image file that we want to upload")
  public ResponseEntity<ImageDto> upload(@PathVariable UUID id,
      @RequestParam("file") MultipartFile file) throws IOException {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ImageMapper.ImagetoImageDto(s3Service.uploadFile(id, file)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    s3Service.deleteFile(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping()
  @Operation(description = "Get list of all images that are saved for one advertisement")
  public ResponseEntity<List<ImageDto>> getAllImages(@RequestParam UUID advertisementId) {
    return ResponseEntity.ok(s3Finder.listFiles(advertisementId));
  }

  @GetMapping("/presigned/{key}")
  @Operation(description = "Get the presigned key for image when given key for image")
  public ResponseEntity<String> getPresignedImage(@PathVariable String key) {
    return ResponseEntity.ok(s3Service.generatePresignedUrl(key, Duration.ofMinutes(5)));
  }

}
