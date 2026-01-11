package io.github.adrian.wieczorek.local_trade.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

  @Getter
  private Key signInKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.signInKey = Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    List<String> roles = getRoles(userDetails);
    extraClaims.put("roles", roles);
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public List<String> getRoles(UserDetails userDetails) {
    List<String> listOfRoles = new ArrayList<>();
    userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
        .forEach(listOfRoles::add);
    return listOfRoles;
  }

  public long getExpirationTime() {
    return jwtExpiration;
  }

  private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails,
      long expiration) {
    return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(signInKey, SignatureAlgorithm.HS256).compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(signInKey).build().parseClaimsJws(token).getBody();
  }
}
