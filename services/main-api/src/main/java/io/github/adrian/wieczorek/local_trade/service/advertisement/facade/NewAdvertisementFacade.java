package io.github.adrian.wieczorek.local_trade.service.advertisement.facade;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.image.ImageEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.image.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewAdvertisementFacade {

  private final AdvertisementService advertisementService;
  private final S3Service s3Service;
  private final UsersRepository usersRepository;
  private final AdvertisementDtoMapper advertisementDtoMapper;
  private final AdvertisementRepository advertisementRepository;

  @Transactional
  public ResponseAdvertisementDto addWholeAdvertisement(RequestAdvertisementDto advertisementDto,
      List<MultipartFile> images, UserDetails userDetails) throws IOException {
    UsersEntity user = usersRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    SimpleAdvertisementResponseDto advertDto = advertisementService.addAd(advertisementDto, user);
    AdvertisementEntity advertisementEntity =
        advertisementRepository.findByAdvertisementId(advertDto.advertisementId())
            .orElseThrow(() -> new EntityNotFoundException("Advertisement not found"));
    if (images != null && !images.isEmpty()) {
      for (MultipartFile imageFile : images) {
        ImageEntity imageEntity =
            s3Service.uploadFile(advertisementEntity.getAdvertisementId(), imageFile);
        advertisementEntity.getImageEntities().add(imageEntity);
      }
    }
    return advertisementDtoMapper.toResponseAdvertisementDto(advertisementEntity);
  }
}
