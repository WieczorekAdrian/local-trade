package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.advertisement.service.AdvertisementService;
import io.github.adrian.wieczorek.local_trade.service.trade.mapper.TradeResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeServiceImpl;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class TradeServiceImplUnitTests {

  @InjectMocks
  TradeServiceImpl tradeService;
  @Mock
  AdvertisementService advertisementService;
  @Mock
  TradeRepository tradeRepository;
  @Mock
  AdvertisementRepository advertisementRepository;
  @Mock
  UsersService usersService;
  @Mock
  TradeResponseDtoMapper tradeResponseDtoMapper;

  private UsersEntity seller;
  private UsersEntity buyer;
  private TradeEntity newTradeEntity;
  private AdvertisementEntity advertisementEntity;
  private UserDetails mockUserDetails;
  private UsersEntity stranger;
  private TradeInitiationRequestDto tradeInitiationRequestDto;
  private TradeResponseDto tradeResponseDto;

  @BeforeEach
  public void setUp() {
    stranger = UserUtils.createUserRoleUser();
    buyer = UserUtils.createUserRoleUser();
    seller = UserUtils.createUserRoleUser();
    buyer.setEmail("buyer@gmail.com");
    seller.setEmail("seller@gmail.com");
    stranger.setEmail("stranger@gmail.com");
    buyer.setId(1);
    seller.setId(2);
    stranger.setId(3);
    mockUserDetails = mock(UserDetails.class);
    advertisementEntity = AdUtils.createAdvertisement();
    advertisementEntity.setUser(seller);
    SimpleUserResponseDto sellerSimpleUserResponseDto =
        new SimpleUserResponseDto(seller.getId(), seller.getEmail());
    SimpleUserResponseDto buyerSimpleUserResponseDto =
        new SimpleUserResponseDto(buyer.getId(), buyer.getEmail());
    SimpleAdvertisementResponseDto simpleAdvertisementResponseDto =
        new SimpleAdvertisementResponseDto(advertisementEntity.getAdvertisementId(),
            advertisementEntity.getTitle());
    tradeInitiationRequestDto = new TradeInitiationRequestDto(BigDecimal.valueOf(2),
        advertisementEntity.getAdvertisementId());
    newTradeEntity = TradeEntity.builder().seller(seller)
        .proposedPrice(tradeInitiationRequestDto.proposedPrice()).buyer(buyer)
        .advertisementEntity(advertisementEntity).status(TradeStatus.PROPOSED)
        .sellerLeftReview(false).buyerLeftReview(false).build();
    tradeResponseDto = new TradeResponseDto(newTradeEntity.getTradeId(), newTradeEntity.getId(),
        newTradeEntity.getStatus(), newTradeEntity.getProposedPrice(),
        newTradeEntity.getCreatedAt(), newTradeEntity.isBuyerMarkedCompleted(),
        newTradeEntity.isSellerMarkedCompleted(), buyerSimpleUserResponseDto,
        sellerSimpleUserResponseDto, simpleAdvertisementResponseDto);

  }

  @AfterEach
  public void tearDown() {
    reset(tradeRepository, tradeResponseDtoMapper);
  }

  @Test
  public void tradeInitiation_thenReturnCreatedTrade() {
    when(tradeResponseDtoMapper.tradeToTradeResponseDto(any())).thenReturn(tradeResponseDto);
    when(mockUserDetails.getUsername()).thenReturn(buyer.getEmail());
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(advertisementService.getCurrentAdvertisement(any())).thenReturn(advertisementEntity);
    when(tradeRepository.existsByAdvertisementEntityAndBuyer(advertisementEntity, buyer))
        .thenReturn(false);
    when(tradeRepository.save(any(TradeEntity.class))).thenReturn(newTradeEntity);

    var trade = tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto);

    verify(tradeRepository, times(1)).save(any(TradeEntity.class));

    Assertions.assertNotNull(trade);
    Assertions.assertEquals(TradeStatus.PROPOSED, trade.status());
    Assertions.assertEquals(buyer.getId(), trade.buyerSimpleDto().id());
    Assertions.assertEquals(seller.getId(), trade.sellerSimpleDto().id());
    Assertions.assertEquals(advertisementEntity.getAdvertisementId(),
        trade.simpleAdvertisementResponseDto().advertisementId());
    Assertions.assertEquals(advertisementEntity.getTitle(),
        trade.simpleAdvertisementResponseDto().title());
    Assertions.assertEquals(newTradeEntity.getProposedPrice(), trade.proposedPrice());
    Assertions.assertEquals(newTradeEntity.getCreatedAt(), trade.createdAt());
    Assertions.assertEquals(newTradeEntity.isBuyerMarkedCompleted(), trade.buyerMarkedCompleted());
    Assertions.assertEquals(newTradeEntity.isSellerMarkedCompleted(),
        trade.sellerMarkedCompleted());
  }

  @Test
  public void tradeInitiationAndProposedPriceIsNull_thenReturnAdvertisementPrice() {
    tradeInitiationRequestDto =
        new TradeInitiationRequestDto(null, advertisementEntity.getAdvertisementId());
    when(tradeResponseDtoMapper.tradeToTradeResponseDto(any())).thenAnswer(invocation -> {
      TradeEntity t = invocation.getArgument(0);
      return new TradeResponseDto(t.getTradeId(), t.getId(), t.getStatus(), t.getProposedPrice(),
          t.getCreatedAt(), t.isBuyerMarkedCompleted(), t.isSellerMarkedCompleted(),
          new SimpleUserResponseDto(t.getBuyer().getId(), t.getBuyer().getEmail()),
          new SimpleUserResponseDto(t.getSeller().getId(), t.getSeller().getEmail()),
          new SimpleAdvertisementResponseDto(t.getAdvertisementEntity().getAdvertisementId(),
              t.getAdvertisementEntity().getTitle()));
    });
    when(mockUserDetails.getUsername()).thenReturn(buyer.getEmail());
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(advertisementService.getCurrentAdvertisement(any())).thenReturn(advertisementEntity);
    when(tradeRepository.existsByAdvertisementEntityAndBuyer(advertisementEntity, buyer))
        .thenReturn(false);
    when(tradeRepository.save(any(TradeEntity.class))).thenReturn(newTradeEntity);

    var trade = tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto);

    verify(tradeRepository, times(1)).save(any(TradeEntity.class));
    Assertions.assertNotNull(trade);
    Assertions.assertEquals(trade.proposedPrice(), advertisementEntity.getPrice());

  }

  @Test
  public void tradeInitiation_whenUserNotFound_thenReturnNotFoundTrade() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername()))
        .thenThrow(new UserNotFoundException(""));
    Assertions.assertThrows(UserNotFoundException.class,
        () -> tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeInitiationWithSameUser_throwsIllegalStateException() {
    when(mockUserDetails.getUsername()).thenReturn(buyer.getEmail());
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);

    when(advertisementService.getCurrentAdvertisement(any())).thenReturn(advertisementEntity);
    advertisementEntity.setUser(buyer);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeInitiationNoAdvertisement_throwsEntityNotFoundException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(advertisementService.getCurrentAdvertisement(any()))
        .thenThrow(new EntityNotFoundException(""));
    Assertions.assertThrows(EntityNotFoundException.class,
        () -> tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeInitiationTradeAlreadyExists_throwsIllegalArgumentException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(advertisementService.getCurrentAdvertisement(any())).thenReturn(advertisementEntity);
    when(tradeRepository.existsByAdvertisementEntityAndBuyer(advertisementEntity, buyer))
        .thenReturn(true);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tradeService.tradeInitiation(mockUserDetails, tradeInitiationRequestDto));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeCancelledButUserIsNotFound_throwsUserNotFoundException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername()))
        .thenThrow(new UserNotFoundException(""));
    Assertions.assertThrows(UserNotFoundException.class,
        () -> tradeService.tradeIsCancelled(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCancelled_thenTradeIsCancelled() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(seller);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    newTradeEntity.setCreatedAt(LocalDateTime.now().minusDays(1));
    tradeService.tradeIsCancelled(mockUserDetails, 2L);
    Assertions.assertEquals(TradeStatus.CANCELLED, newTradeEntity.getStatus());
    verify(tradeRepository, times(1)).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCancelled_whenTradeIsCompleted_throwsIllegalStateException() {
    newTradeEntity.setStatus(TradeStatus.COMPLETED);
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(seller);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tradeService.tradeIsCancelled(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCancelled_byUnauthorizedUser_throwsSecurityException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(stranger);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    Assertions.assertThrows(SecurityException.class,
        () -> tradeService.tradeIsCancelled(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCancelledAndItsTooSoon_throwsIllegalArgumentException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(seller);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    newTradeEntity.setCreatedAt(LocalDateTime.now());
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tradeService.tradeIsCancelled(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCompleted_thenTradeIsCompleted() {
    newTradeEntity.setSellerMarkedCompleted(true);
    newTradeEntity.setCreatedAt(LocalDateTime.now().minusDays(1));

    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));

    newTradeEntity.setCreatedAt(LocalDateTime.now().minusDays(1));
    tradeService.tradeIsComplete(mockUserDetails, 2L);
    Assertions.assertEquals(TradeStatus.COMPLETED, newTradeEntity.getStatus());
    Assertions.assertTrue(newTradeEntity.isBuyerMarkedCompleted());
    verify(tradeRepository, times(1)).save(newTradeEntity);
  }

  @Test
  public void tradeIsCompletedAndTradeStatusIsWrong_throwsIllegalStateException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    newTradeEntity.setStatus(TradeStatus.PROCESSING);
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> tradeService.tradeIsComplete(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCompletedUserNotFound_throwsUserNotFoundException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername()))
        .thenThrow(new UserNotFoundException(""));
    Assertions.assertThrows(UserNotFoundException.class,
        () -> tradeService.tradeIsComplete(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCompletedTradeIsNotFound_throwsEntityNotFoundException() {
    when(tradeRepository.findById(any())).thenReturn(Optional.empty());
    Assertions.assertThrows(EntityNotFoundException.class,
        () -> tradeService.tradeIsComplete(mockUserDetails, 2L));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void tradeIsCompletedAndItsTooSoon_thenTradeStatusIsNotChanged() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(tradeRepository.findById(any())).thenReturn(Optional.of(newTradeEntity));
    newTradeEntity.setBuyerMarkedCompleted(true);
    newTradeEntity.setSellerMarkedCompleted(true);
    newTradeEntity.setCreatedAt(LocalDateTime.now());
    tradeService.tradeIsComplete(mockUserDetails, 2L);
    Assertions.assertEquals(TradeStatus.PROPOSED, newTradeEntity.getStatus());
    verify(tradeRepository, times(1)).save(any(TradeEntity.class));
  }

}
