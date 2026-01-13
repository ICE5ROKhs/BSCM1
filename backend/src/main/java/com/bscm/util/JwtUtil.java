package com.bscm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /** 生成JWT token */
  public String generateToken(Long userId, String phone) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("phone", phone);

    return Jwts.builder()
        .claims(claims)
        .subject(phone)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  /** 从token中获取用户ID */
  public Long getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("userId", Long.class);
  }

  /** 从token中获取手机号 */
  public String getPhoneFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("phone", String.class);
  }

  /** 从token中获取Claims */
  private Claims getClaimsFromToken(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  /** 验证token是否有效 */
  public boolean validateToken(String token) {
    try {
      Claims claims = getClaimsFromToken(token);
      Date expiration = claims.getExpiration();
      Date now = new Date();
      boolean isValid = !expiration.before(now);

      if (!isValid) {
        log.warn("Token已过期，过期时间: {}, 当前时间: {}", expiration, now);
      } else {
        log.debug("Token验证成功，过期时间: {}", expiration);
      }

      return isValid;
    } catch (Exception e) {
      log.error("Token验证失败: {}", e.getMessage(), e);
      return false;
    }
  }
}
