package io.github.adrian.wieczorek.local_trade.security;


import io.github.adrian.wieczorek.local_trade.service.refreshtoken.service.RefreshTokenService;
import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.user.dto.RegisterUsersDto;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.user.facade.UserEventFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserEventFacade userEventFacade;
    private final JwtService jwtService;
    private final JwtBlacklistService jwtBlacklistService;
    private final RefreshTokenService refreshTokenService;



    @Override
    @Transactional
    public UsersEntity signup(RegisterUsersDto dto) {
        UsersEntity user = new UsersEntity();
                user.setName(dto.getName());
                user.setEmail(dto.getEmail());
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        userEventFacade.publishUserRegistered(user);
        return user;
    }

    @Override
    @Transactional
    public UsersEntity authenticate(LoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getEmail(),
                        dto.getPassword()
                )
        );
        return userRepository.findByEmail(dto.getEmail())
                .orElseThrow();
    }
    @Override
    @Transactional
    public List<String> getAuthenticatedRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UsersEntity currentUser = (UsersEntity) auth.getPrincipal();
        List<String> listOfRoles = new ArrayList<>();
        currentUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).forEach(listOfRoles::add);
        return listOfRoles;
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {

        if (accessToken != null) {
            String userEmail = jwtService.extractUsername(accessToken);
            log.info("Processing logout attempt for user {}", userEmail);

            Date expiration = jwtService.extractExpiration(accessToken);
            long now = System.currentTimeMillis();
            long ttlInSeconds = (expiration.getTime() - now) / 1000;
            if (ttlInSeconds > 0) {
                jwtBlacklistService.blacklistToken(accessToken, ttlInSeconds);
                log.info("Access token for user {} blacklisted for {} seconds", userEmail, ttlInSeconds);
            }
        } else {
            log.warn("No access token provided, skipping blacklist");
        }

        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
    }

}

