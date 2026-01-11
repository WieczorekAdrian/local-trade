package io.github.adrian.wieczorek.local_trade.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.controller.TradeController;
import io.github.adrian.wieczorek.local_trade.enums.TradeStatus;
import io.github.adrian.wieczorek.local_trade.security.JwtBlacklistService;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementEntity;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeInitiationRequestDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeStatusRequestDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.SimpleUserResponseDto;
import io.github.adrian.wieczorek.local_trade.service.trade.dto.TradeResponseDto;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.service.trade.service.TradeService;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TradeController.class)
public class TradeControllerUnitTests {

  @MockitoBean
  JwtService jwtService;
  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  TradeService tradeService;
  @MockitoBean
  JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean
  JwtBlacklistService jwtBlacklistService;

  @Autowired
  private ObjectMapper objectMapper;

  private AdvertisementEntity mockAdvertisementEntity;
  private SimpleUserResponseDto buyer;
  private SimpleUserResponseDto seller;
  private SimpleAdvertisementResponseDto simpleAdvertisementResponseDto;

  @BeforeEach
  public void setUp() {
    mockAdvertisementEntity = AdUtils.createAdvertisement();
    mockAdvertisementEntity.setAdvertisementId(UUID.randomUUID());
    buyer = new SimpleUserResponseDto(1, "buyer@test.com");
    seller = new SimpleUserResponseDto(2, "seller@test.com");
    simpleAdvertisementResponseDto = new SimpleAdvertisementResponseDto(
        mockAdvertisementEntity.getAdvertisementId(), mockAdvertisementEntity.getTitle());
  }

  @Test
  @WithMockUser("buyer@test.com")
  public void tradeInitiation_thenTradeIsInitiated_returnsTradeResponse() throws Exception {
    TradeInitiationRequestDto mockRequest = new TradeInitiationRequestDto(BigDecimal.valueOf(2),
        mockAdvertisementEntity.getAdvertisementId());
    TradeResponseDto mockResponse =
        new TradeResponseDto(UUID.randomUUID(), 1L, TradeStatus.PROPOSED, BigDecimal.valueOf(2),
            LocalDateTime.now(), false, false, buyer, seller, simpleAdvertisementResponseDto);

    when(tradeService.tradeInitiation(any(UserDetails.class), (eq(mockRequest))))
        .thenReturn(mockResponse);

    String requestDtoToString = objectMapper.writeValueAsString(mockRequest);

    mockMvc
        .perform(post("/trades").contentType(MediaType.APPLICATION_JSON).content(requestDtoToString)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(mockResponse.id()))
        .andExpect(jsonPath("$.status").value("PROPOSED"))
        .andExpect(jsonPath("$.proposedPrice").value(mockResponse.proposedPrice()))
        .andExpect(jsonPath("$.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.sellerMarkedCompleted").value(mockResponse.sellerMarkedCompleted()))
        .andExpect(jsonPath("$.buyerMarkedCompleted").value(mockResponse.buyerMarkedCompleted()));

    verify(tradeService, times(1)).tradeInitiation(any(UserDetails.class),
        any(TradeInitiationRequestDto.class));

  }

  @Test
  @WithMockUser("buyer@test.com")
  public void tradeInitiationWithBadRequest_returnsBadRequest() throws Exception {
    TradeInitiationRequestDto mockRequest =
        new TradeInitiationRequestDto(BigDecimal.valueOf(2), null);

    String requestDtoToString = objectMapper.writeValueAsString(mockRequest);

    mockMvc.perform(post("/trades").contentType(MediaType.APPLICATION_JSON)
        .content(requestDtoToString).with(csrf())).andExpect(status().isBadRequest());

    verify(tradeService, never()).tradeInitiation(any(UserDetails.class),
        any(TradeInitiationRequestDto.class));

  }

  @Test
  @WithMockUser("buyer@test.com")
  public void updateTradeStatusToCompleted_thenTradeStatusIsUpdated() throws Exception {
    TradeStatusRequestDto tradeStatusRequestDto = new TradeStatusRequestDto(TradeStatus.COMPLETED);

    TradeResponseDto mockResponse =
        new TradeResponseDto(UUID.randomUUID(), 1L, TradeStatus.COMPLETED, BigDecimal.valueOf(2),
            LocalDateTime.now(), false, false, buyer, seller, simpleAdvertisementResponseDto);

    when(tradeService.updateTradeStatus(any(UserDetails.class), any(Long.class),
        any(TradeStatus.class))).thenReturn(mockResponse);

    String tradeStatusDtoToString = objectMapper.writeValueAsString(tradeStatusRequestDto);

    mockMvc
        .perform(patch("/trades/" + mockResponse.id()).contentType(MediaType.APPLICATION_JSON)
            .content(tradeStatusDtoToString).with(csrf()))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(mockResponse.id()))
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    verify(tradeService, times(1)).updateTradeStatus(any(UserDetails.class), any(Long.class),
        any(TradeStatus.class));

  }

  @Test
  @WithMockUser("buyer@test.com")
  public void updateTradeStatusToCancelled_thenTradeStatusIsUpdated() throws Exception {
    TradeStatusRequestDto tradeStatusRequestDto = new TradeStatusRequestDto(TradeStatus.CANCELLED);

    TradeResponseDto mockResponse =
        new TradeResponseDto(UUID.randomUUID(), 1L, TradeStatus.CANCELLED, BigDecimal.valueOf(2),
            LocalDateTime.now(), true, true, buyer, seller, simpleAdvertisementResponseDto);

    when(tradeService.updateTradeStatus(any(UserDetails.class), any(Long.class),
        any(TradeStatus.class))).thenReturn(mockResponse);

    String tradeStatusDtoToString = objectMapper.writeValueAsString(tradeStatusRequestDto);

    mockMvc
        .perform(patch("/trades/" + mockResponse.id()).contentType(MediaType.APPLICATION_JSON)
            .content(tradeStatusDtoToString).with(csrf()))
        .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(mockResponse.id()))
        .andExpect(jsonPath("$.status").value("CANCELLED"));

    verify(tradeService, times(1)).updateTradeStatus(any(UserDetails.class), any(Long.class),
        any(TradeStatus.class));

  }

  @Test
  @WithMockUser("buyer@test.com")
  public void updateTradeStatusToCompletedAndTradeStatusIsNull_thenTradeStatusIsNotUpdated_returnsBadRequest()
      throws Exception {
    TradeStatusRequestDto tradeStatusRequestDto = new TradeStatusRequestDto(null);

    String tradeStatusDtoToString = objectMapper.writeValueAsString(tradeStatusRequestDto);

    mockMvc.perform(patch("/trades/" + 1L).contentType(MediaType.APPLICATION_JSON)
        .content(tradeStatusDtoToString).with(csrf())).andExpect(status().isBadRequest());

    verify(tradeService, never()).updateTradeStatus(any(UserDetails.class), any(Long.class),
        any(TradeStatus.class));
  }
}
