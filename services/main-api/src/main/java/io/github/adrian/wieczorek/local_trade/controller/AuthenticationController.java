package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginDto;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.dto.RefreshTokenRequest;
import io.github.adrian.wieczorek.local_trade.service.user.dto.RegisterUsersDto;
import io.github.adrian.wieczorek.local_trade.service.user.facade.LoginFacade;

import io.github.adrian.wieczorek.local_trade.service.user.dto.LoginResponse;
import io.github.adrian.wieczorek.local_trade.security.AuthenticationService;
import io.github.adrian.wieczorek.local_trade.service.refreshtoken.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final LoginFacade loginFacade;

    @Value("${isCookieSecure}")
    private boolean isCookieSecure;


    @PostMapping("/signup")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUsersDto registerUserDto) {
        authenticationService.signup(registerUserDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody @Valid LoginDto loginUserDto) {
        LoginResponse loginResponse = loginFacade.authenticateAndAssignNewRefreshToken(loginUserDto);
        ResponseCookie jwtCookie = ResponseCookie.from("refreshToken",loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/auth/refresh")
                .maxAge(7*24*60*60)
                .build();
        loginResponse.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "Refresh token for users when jwt token expires")
    public ResponseEntity<LoginResponse> refreshToken(@CookieValue(name = "refreshToken") String refreshTokenFromCookie) {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setToken(refreshTokenFromCookie);
        return ResponseEntity.ok(refreshTokenService.generateNewTokenFromRefresh(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(HttpServletRequest request, @CookieValue(name = "refreshToken") String refreshToken) {

        authenticationService.logout(request.getHeader("Authorization"), refreshToken);
        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }
}