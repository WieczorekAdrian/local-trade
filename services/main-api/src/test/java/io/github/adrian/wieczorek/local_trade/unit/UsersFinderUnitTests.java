package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.exceptions.UserNotFoundException;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.user.dto.AdminUserViewDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.UserDashboardResponseDto;
import io.github.adrian.wieczorek.local_trade.service.user.mapper.AdminUserViewDtoMapper;
import io.github.adrian.wieczorek.local_trade.service.user.mapper.UserDashboardResponseMapper;
import io.github.adrian.wieczorek.local_trade.service.user.service.UsersFinder;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UsersFinderUnitTests {
    @InjectMocks
    private UsersFinder usersFinder;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private AdminUserViewDtoMapper adminUserViewDtoMapper;
    @Mock
    private UserDashboardResponseMapper userDashboardResponseMapper;

    @Test
    public void allUsers_shouldReturnMappedDtos() {

        UsersEntity user1 = UserUtils.createUserRoleUser();
        UsersEntity user2 = UserUtils.createUserRoleUser();
        List<UsersEntity> mockEntities = List.of(user1, user2);

        AdminUserViewDto dto1 = new AdminUserViewDto(user1.getId(), "Adam", user1.getRole());
        AdminUserViewDto dto2 = new AdminUserViewDto(user2.getId(), "Borys", user2.getRole());

        List<AdminUserViewDto> expectedDtos = List.of(dto1, dto2);


        when(usersRepository.findAll()).thenReturn(mockEntities);

        when(adminUserViewDtoMapper.toAdminUserViewDto(user1)).thenReturn(dto1);
        when(adminUserViewDtoMapper.toAdminUserViewDto(user2)).thenReturn(dto2);

        List<AdminUserViewDto> actualDtos = usersFinder.allUsers();

        assertEquals(expectedDtos.size(), actualDtos.size(), "List is the same size");
        assertEquals(expectedDtos.get(0).name(), actualDtos.get(0).name(), "First dto is mapped good");

        verify(usersRepository, times(1)).findAll();

        verify(adminUserViewDtoMapper, times(2)).toAdminUserViewDto(any());
    }
    @Test
    void allUsers_shouldReturnEmptyList_whenNoUsersFound() {
        when(usersRepository.findAll()).thenReturn(Collections.emptyList());

        List<AdminUserViewDto> actualDtos = usersFinder.allUsers();


        assertTrue(actualDtos.isEmpty(), "Dto list is empty");

        verify(usersRepository, times(1)).findAll();

        verifyNoInteractions(adminUserViewDtoMapper);
    }

    @Test
    void allUsers_shouldThrowRuntimeException_whenRepositoryFails() {

        String errorMessage = "Database connection failed.";
        when(usersRepository.findAll()).thenThrow(new RuntimeException(errorMessage));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> usersFinder.allUsers(),
                "Method should have thrown an db exception"
        );

        assertEquals(errorMessage, thrown.getMessage());

        verifyNoInteractions(adminUserViewDtoMapper);
    }
    @Test
    void getLoggedInUser_shouldReturnMappedDto_whenUserIsFound() {

        UsersEntity mockEntity =  UserUtils.createUserRoleUser();
        UserDashboardResponseDto expectedDto = new UserDashboardResponseDto("test@test.com", 3, 2,"ROLE_USER",UUID.randomUUID(),mockEntity.getName());

        when(usersRepository.findByEmail(mockEntity.getEmail())).thenReturn(Optional.of(mockEntity));
        when(userDashboardResponseMapper.toDto(mockEntity)).thenReturn(expectedDto);

        UserDashboardResponseDto actualDto = usersFinder.getLoggedInUser(mockEntity.getEmail());

        assertNotNull(actualDto);
        assertEquals(expectedDto.email(), actualDto.email());

        verify(usersRepository, times(1)).findByEmail(mockEntity.getEmail());
        verify(userDashboardResponseMapper, times(1)).toDto(mockEntity);
    }

    @Test
    void getLoggedInUser_shouldThrowUserNotFoundException_whenUserIsNotFound() {
        UsersEntity mockEntity = UserUtils.createUserRoleUser();

        when(usersRepository.findByEmail(mockEntity.getEmail())).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> usersFinder.getLoggedInUser(mockEntity.getEmail()),
                "Should throw UserNotFoundException when called"
        );

        verify(usersRepository, times(1)).findByEmail(mockEntity.getEmail());
        verifyNoInteractions(userDashboardResponseMapper);
    }

    @Test
    void getLoggedInUser_shouldThrowException_whenMappingFails() {

        UsersEntity mockEntity = UserUtils.createUserRoleUser();

        when(usersRepository.findByEmail(mockEntity.getEmail())).thenReturn(Optional.of(mockEntity));

        when(userDashboardResponseMapper.toDto(any())).thenThrow(new RuntimeException("Mapping failure"));

        assertThrows(
                RuntimeException.class,
                () -> usersFinder.getLoggedInUser(mockEntity.getEmail()),
                "Exception should have been thrown with RuntimeException"
        );

        verify(usersRepository, times(1)).findByEmail(mockEntity.getEmail());
        verify(userDashboardResponseMapper, times(1)).toDto(mockEntity);
    }
}
