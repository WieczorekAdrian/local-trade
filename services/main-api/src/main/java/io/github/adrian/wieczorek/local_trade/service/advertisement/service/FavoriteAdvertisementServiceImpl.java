package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.FavoriteAdvertisementMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteAdvertisementServiceImpl implements FavoriteAdvertisementService {

  private final AdvertisementRepository advertisementRepository;
  private final FavoriteAdvertisementMapper favoriteAdvertisementMapper;
  private final UsersService usersService;

  @Transactional
  @Override
  public void addFavoriteAdvertisement(String email, UUID advertisementId) {
    UsersEntity user = usersService.getCurrentUser(email);
    AdvertisementEntity ad = advertisementRepository.findByAdvertisementId(advertisementId)
        .orElseThrow(() -> new EntityNotFoundException("Advertisement not found "));
    Set<UsersEntity> favoritedByUsers = ad.getFavoritedByUsers();
    Set<AdvertisementEntity> favoritedByAdvertisementEntity =
        user.getFavoritedAdvertisementEntities();
    favoritedByUsers.add(user);
    favoritedByAdvertisementEntity.add(ad);
    advertisementRepository.save(ad);
    usersService.saveUser(user);
  }

  @Override
  @Transactional
  public void deleteFavoriteAdvertisement(String email, UUID advertisementId) {
    UsersEntity user = usersService.getCurrentUser(email);
    AdvertisementEntity ad = advertisementRepository.findByAdvertisementId(advertisementId)
        .orElseThrow(() -> new EntityNotFoundException("Advertisement not found "));
    ad.getFavoritedByUsers().remove(user);
    user.getFavoritedAdvertisementEntities().remove(ad);
    advertisementRepository.save(ad);
    usersService.saveUser(user);
  }
}
