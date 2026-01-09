package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.AdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.category.service.CategoryService;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementMapperToAdvertisementUpdateDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.SimpleAdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryService categoryService;
    private final UsersService usersService;
    private final AdvertisementMapper advertisementMapper;
    private final SimpleAdvertisementDtoMapper simpleAdvertisementDtoMapper;

    @Override
    @Transactional
    public SimpleAdvertisementResponseDto addAd(RequestAdvertisementDto dto, UserDetails userDetails) {
        log.info("Attempting to add advertisement for user {}", userDetails.getUsername());
        UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());
        log.debug("User with id found with email {}", user.getEmail());
        CategoryEntity category = categoryService.getCategoryEntityById(dto.categoryId());
        log.debug("Category with id found with name {}", category.getName());
        AdvertisementEntity ad = AdvertisementEntity.builder()
                .categoryEntity(category)
                .price(dto.price())
                .title(dto.title())
                .image(dto.image())
                .description(dto.description())
                .active(dto.active())
                .location(dto.location())
                .user(user)
                .build();

         AdvertisementEntity savedAd = advertisementRepository.save(ad);
         log.info("Successfully added advertisement for user {}", userDetails.getUsername());

         return simpleAdvertisementDtoMapper.advertisementToSimpleDto(savedAd);
    }

    @Override
    @Transactional
    public AdvertisementUpdateDto changeAdvertisement(AdvertisementUpdateDto dto, UserDetails userDetails, Integer advertisementId) {
        log.info("Attempting to change advertisement for user {}", userDetails.getUsername());
        UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());
        log.debug("User with id found with email:  {}", user.getEmail());
        AdvertisementEntity ad = advertisementRepository.findByUserAndId(user, advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement not found"));
        log.debug("Advertisement found with UUID: {}", advertisementId);
        if (!ad.getUser().equals(user)) {
            throw new AccessDeniedException("Access denied for user "+user.getUsername());
        }
        advertisementMapper.updateAdvertisementFromDtoSkipNull(dto, ad);
        AdvertisementUpdateDto updatedDto = AdvertisementMapperToAdvertisementUpdateDto.toDto(ad);
        advertisementRepository.save(ad);
        log.info("Successfully changed advertisement for user {}", userDetails.getUsername());
        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteAdvertisement(UserDetails userDetails, Integer advertisementId) {
        log.info("Attempting to delete advertisement with id {}", advertisementId);
        UsersEntity user = usersService.getCurrentUser(userDetails.getUsername());
        log.debug("User found with id {}", user.getId());
        AdvertisementEntity ad = advertisementRepository.findByUserAndId(user, advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement not found"));
        log.debug("Advertisement with id {} has been found", advertisementId);
        advertisementRepository.delete(ad);
        log.info("Deleted advertisement with id {}", advertisementId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deactivateOldAdvertisements() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        log.info("Rozpoczynam wygaszanie ogłoszeń starszych niż: {}", cutoffDate);
        int updatedCount = advertisementRepository.deactivateExpiredAds(cutoffDate);
        log.info("Zakończono. Wygaszono (ustawiono active=false) dla {} ogłoszeń.", updatedCount);
    }

    @Override
    @Transactional
    public AdvertisementEntity getCurrentAdvertisement(UUID advertisementId){
        log.info("Attempting to get advertisement with id {}", advertisementId);
        var advertisementEntity = advertisementRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement not found with id " + advertisementId));
        log.debug("Advertisement found with id {}", advertisementId);
        return advertisementEntity;
    }
}