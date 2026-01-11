package io.github.adrian.wieczorek.local_trade.security;

import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public interface TestJwtUtils {
  static String expiredToken(JwtService jwtService, UsersEntity user) {
    Date expiredDate = new Date(System.currentTimeMillis() - 10_000); // 10s temu

    return Jwts.builder().setSubject(user.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis() - 20_000)).setExpiration(expiredDate)
        .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256).compact();
  }

  static String generateTokenWithCustomExpiration(JwtService jwtService, UsersEntity user) {
    return Jwts.builder().setSubject(user.getUsername()).setIssuedAt(new Date()) // <- NOWY czas
                                                                                 // wydania
        .setExpiration(new Date(System.currentTimeMillis() + 20_000))
        .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256).compact();
  }

  static String generateToken(JwtService jwtService, UsersEntity user) {
    return Jwts.builder().setSubject(user.getUsername()).setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 20_000))
        .signWith(jwtService.getSignInKey(), SignatureAlgorithm.HS256).compact();
  }
}
