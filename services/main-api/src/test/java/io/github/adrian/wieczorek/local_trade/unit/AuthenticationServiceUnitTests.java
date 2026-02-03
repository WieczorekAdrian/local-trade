package io.github.adrian.wieczorek.local_trade.unit;

import io.github.adrian.wieczorek.local_trade.security.AuthenticationServiceImpl;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceUnitTests {

  @InjectMocks
  private AuthenticationServiceImpl authenticationService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getAuthenticatedRoles_ShouldReturnListOfRoleStrings_WhenUserIsAuthenticated() {
    String roleName = "ROLE_USER";

    UsersEntity mockUser = mock(UsersEntity.class);
    GrantedAuthority authority = new SimpleGrantedAuthority(roleName);

    doReturn(List.of(authority)).when(mockUser).getAuthorities();

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(mockUser);

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    List<String> result = authenticationService.getAuthenticatedRoles();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(roleName, result.get(0));
  }
}
