package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.FavoriteAdvertisementServiceImpl;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FavoriteAdvertisementEntityTests {

  @Mock
  UsersService usersService;
  @Mock
  AdvertisementRepository advertisementRepository;
  @InjectMocks
  FavoriteAdvertisementServiceImpl favoriteAdvertisementService;

  @Test
  public void whenAddingFavoriteAdvertisement_thenSuccess() {
    UsersEntity user = UserUtils.createUserRoleUser();
    AdvertisementEntity ad = AdUtils.createAdvertisement();
    when(usersService.getCurrentUser(user.getEmail())).thenReturn(user);
    when(advertisementRepository.findByAdvertisementId(ad.getAdvertisementId()))
        .thenReturn(Optional.of(ad));

    favoriteAdvertisementService.addFavoriteAdvertisement(user.getEmail(), ad.getAdvertisementId());
    verify(advertisementRepository).save(ad);
    assertTrue(ad.getFavoritedByUsers().contains(user));
    assertEquals(1, ad.getFavoritedByUsers().size());
  }

  @Test
  public void whenAddingFavoriteAdvertisement_thenNoUserWithThatUserNameFound() {
    UsersEntity user = UserUtils.createUserRoleUser();
    user.setName("user");
    AdvertisementEntity ad = AdUtils.createAdvertisement();
    when(usersService.getCurrentUser(user.getEmail())).thenThrow(UserNotFoundException.class);

    assertThrows(UserNotFoundException.class, () -> {
      favoriteAdvertisementService.addFavoriteAdvertisement(user.getEmail(),
          ad.getAdvertisementId());
    });
    verify(advertisementRepository, never()).save(any(AdvertisementEntity.class));
  }

  @Test
  public void whenAddingFavoriteAdvertisement_thenAdvertisementNotFound() {
    UsersEntity user = UserUtils.createUserRoleUser();
    AdvertisementEntity ad = AdUtils.createAdvertisement();
    when(usersService.getCurrentUser(user.getEmail())).thenReturn(user);
    when(advertisementRepository.findByAdvertisementId(ad.getAdvertisementId()))
        .thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> {
      favoriteAdvertisementService.addFavoriteAdvertisement(user.getEmail(),
          ad.getAdvertisementId());
    });
    verify(advertisementRepository, never()).save(any(AdvertisementEntity.class));
  }

  @Test
  public void whenDeletingFavoriteAdvertisement_thenSuccess() {
    UsersEntity user = UserUtils.createUserRoleUser();
    AdvertisementEntity ad = AdUtils.createAdvertisement();

    user.getFavoritedAdvertisementEntities().add(ad);
    ad.getFavoritedByUsers().add(user);

    assertEquals(1, ad.getFavoritedByUsers().size());
    assertTrue(ad.getFavoritedByUsers().contains(user));

    when(usersService.getCurrentUser(user.getEmail())).thenReturn(user);
    when(advertisementRepository.findByAdvertisementId(ad.getAdvertisementId()))
        .thenReturn(Optional.of(ad));

    favoriteAdvertisementService.deleteFavoriteAdvertisement(user.getEmail(),
        ad.getAdvertisementId());
    verify(advertisementRepository).save(ad);
    Assertions.assertFalse(ad.getFavoritedByUsers().contains(user));
    assertEquals(0, ad.getFavoritedByUsers().size());
  }

  @Test
  public void whenDeletingFavoriteAdvertisement_thenUserNotFound() {
    UsersEntity user = UserUtils.createUserRoleUser();
    AdvertisementEntity ad = AdUtils.createAdvertisement();

    when(usersService.getCurrentUser(user.getEmail())).thenThrow(UserNotFoundException.class);

    assertThrows(UserNotFoundException.class, () -> {
      favoriteAdvertisementService.deleteFavoriteAdvertisement(user.getEmail(),
          ad.getAdvertisementId());
    });
    verify(advertisementRepository, never()).save(any(AdvertisementEntity.class));
  }

  @Test
  public void whenDeletingFavoriteAdvertisement_thenAdvertisementNotFound() {
    UsersEntity user = UserUtils.createUserRoleUser();
    AdvertisementEntity ad = AdUtils.createAdvertisement();

    when(usersService.getCurrentUser(user.getEmail())).thenReturn(user);
    when(advertisementRepository.findByAdvertisementId(ad.getAdvertisementId()))
        .thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> {
      favoriteAdvertisementService.deleteFavoriteAdvertisement(user.getEmail(),
          ad.getAdvertisementId());
    });
    verify(advertisementRepository, never()).save(any(AdvertisementEntity.class));
  }

}
