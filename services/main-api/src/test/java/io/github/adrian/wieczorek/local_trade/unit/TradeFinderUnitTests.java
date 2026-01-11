package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.TradeRepository;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.mapper.TradeResponseDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeFinder;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersService;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeFinderUnitTests {
  @Mock
  UsersService usersService;
  @InjectMocks
  TradeFinder tradeFinder;
  @Mock
  TradeRepository tradeRepository;
  @Mock
  TradeResponseDtoMapper tradeResponseDtoMapper;

  private UsersEntity buyer;
  private TradeEntity newTradeEntity;
  private UserDetails mockUserDetails;
  private TradeResponseDto tradeResponseDto;

  @BeforeEach
  public void setUp() {
    UsersEntity stranger = UserUtils.createUserRoleUser();
    buyer = UserUtils.createUserRoleUser();
    UsersEntity seller = UserUtils.createUserRoleUser();
    buyer.setEmail("buyer@gmail.com");
    seller.setEmail("seller@gmail.com");
    stranger.setEmail("stranger@gmail.com");
    buyer.setId(1);
    seller.setId(2);
    stranger.setId(3);
    mockUserDetails = mock(UserDetails.class);
    AdvertisementEntity advertisementEntity = AdUtils.createAdvertisement();
    advertisementEntity.setUser(seller);
    SimpleUserResponseDto sellerSimpleUserResponseDto =
        new SimpleUserResponseDto(seller.getId(), seller.getEmail());
    SimpleUserResponseDto buyerSimpleUserResponseDto =
        new SimpleUserResponseDto(buyer.getId(), buyer.getEmail());
    SimpleAdvertisementResponseDto simpleAdvertisementResponseDto =
        new SimpleAdvertisementResponseDto(advertisementEntity.getAdvertisementId(),
            advertisementEntity.getTitle());
    TradeInitiationRequestDto tradeInitiationRequestDto = new TradeInitiationRequestDto(
        BigDecimal.valueOf(2), advertisementEntity.getAdvertisementId());
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

  @Test
  public void getAllMyTradesWithNoUsers_throwsUserNotFoundException() {
    when(usersService.getCurrentUser(mockUserDetails.getUsername()))
        .thenThrow(new UserNotFoundException(""));
    Assertions.assertThrows(UserNotFoundException.class,
        () -> tradeFinder.getAllMyTrades(mockUserDetails));
    verify(tradeRepository, never()).save(any(TradeEntity.class));
  }

  @Test
  public void getAllMyTrades_returnsAllTradesIntoAList() {
    List<TradeEntity> tradeEntities = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tradeEntities.add(newTradeEntity);
    }

    when(mockUserDetails.getUsername()).thenReturn(buyer.getUsername());
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(tradeRepository.findAllByBuyerOrSeller(any(UsersEntity.class), any(UsersEntity.class)))
        .thenReturn(tradeEntities);
    when(tradeResponseDtoMapper.tradeToTradeResponseDto(any(TradeEntity.class)))
        .thenReturn(tradeResponseDto);

    List<TradeResponseDto> result = tradeFinder.getAllMyTrades(mockUserDetails);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(tradeEntities.size(), result.size());
    Assertions.assertEquals(tradeEntities.get(0).getId(), result.get(0).id());
  }

  @Test
  public void getAllMyTrades_returnsEmptyList() {
    List<TradeEntity> tradeEntities = new ArrayList<>();

    when(mockUserDetails.getUsername()).thenReturn(buyer.getUsername());
    when(usersService.getCurrentUser(mockUserDetails.getUsername())).thenReturn(buyer);
    when(tradeRepository.findAllByBuyerOrSeller(any(UsersEntity.class), any(UsersEntity.class)))
        .thenReturn(tradeEntities);

    List<TradeResponseDto> result = tradeFinder.getAllMyTrades(mockUserDetails);

    Assertions.assertNotNull(result);
    verify(tradeResponseDtoMapper, never()).tradeToTradeResponseDto(any(TradeEntity.class));
  }
}
