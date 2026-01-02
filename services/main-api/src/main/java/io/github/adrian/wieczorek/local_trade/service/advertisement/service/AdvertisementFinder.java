package io.github.adrian.wieczorek.local_trade.service.advertisement.service;

import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.ResponseAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.SimpleAdvertisementResponseDto;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.AdvertisementDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.advertisement.mapper.SimpleAdvertisementDtoMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementFinder {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementDtoMapper advertisementDtoMapper;
    private final SimpleAdvertisementDtoMapper simpleAdvertisementDtoMapper;

    public ResponseAdvertisementDto getAdvertisementById(UUID advertisementId) {
        var advertisement  = advertisementRepository.findByAdvertisementId(advertisementId)
                .orElseThrow(() -> new EntityNotFoundException("Advertisement not found with id: " + advertisementId));
        return advertisementDtoMapper.toResponseAdvertisementDto(advertisement);
    }

    @Transactional(readOnly = true)
    public List<SimpleAdvertisementResponseDto> findAllAdvertisementsByCategoryId(Integer categoryId) {
            return advertisementRepository.findByCategoryEntityId(categoryId)
                    .stream()
                    .map(simpleAdvertisementDtoMapper::advertisementToSimpleDto)
                    .toList();
    }
}
