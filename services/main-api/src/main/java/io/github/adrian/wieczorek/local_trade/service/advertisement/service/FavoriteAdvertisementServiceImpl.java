package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.FavoriteAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.FavoriteAdvertisementMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteAdvertisementServiceImpl implements FavoriteAdvertisementService {

  private final AdvertisementRepository advertisementRepository;
  private final UsersRepository usersRepository;
  private final FavoriteAdvertisementMapper favoriteAdvertisementMapper;

  @Override
  @Transactional(readOnly = true)
  public Set<FavoriteAdvertisementDto> getFavoriteAdvertisements(UserDetails userDetails) {
    return usersRepository.findByEmail(userDetails.getUsername())
        .map(users -> users.getFavoritedAdvertisementEntities().stream()
            .map(favoriteAdvertisementMapper::toDto).collect(Collectors.toSet()))
        .orElseThrow(() -> new UserNotFoundException(
            "No user found with username: " + userDetails.getUsername()));
  }

  @Transactional
  @Override
  public void addFavoriteAdvertisement(UserDetails userDetails, UUID advertisementId) {
    UsersEntity user = getUser(userDetails);
    AdvertisementEntity ad = advertisementRepository.findByAdvertisementId(advertisementId)
        .orElseThrow(() -> new EntityNotFoundException("Advertisement not found "));
    Set<UsersEntity> favoritedByUsers = ad.getFavoritedByUsers();
    Set<AdvertisementEntity> favoritedByAdvertisementEntity =
        user.getFavoritedAdvertisementEntities();
    favoritedByUsers.add(user);
    favoritedByAdvertisementEntity.add(ad);
    advertisementRepository.save(ad);
    usersRepository.save(user);
  }

  @Override
  @Transactional
  public void deleteFavoriteAdvertisement(UserDetails userDetails, UUID advertisementId) {
    UsersEntity user = getUser(userDetails);
    AdvertisementEntity ad = advertisementRepository.findByAdvertisementId(advertisementId)
        .orElseThrow(() -> new EntityNotFoundException("Advertisement not found "));
    ad.getFavoritedByUsers().remove(user);
    user.getFavoritedAdvertisementEntities().remove(ad);
    advertisementRepository.save(ad);
    usersRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UsersEntity getUser(UserDetails userDetails) {
    return usersRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException(
            "No user found with username: " + userDetails.getUsername()));
  }

}
