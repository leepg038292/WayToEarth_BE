package com.waytoearth.service.auth;

import com.waytoearth.entity.enums.UserRole;
import com.waytoearth.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {


    private final SecretKey secretKey;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long expiration) {
        log.info("JWT configuration initialized with expiration: {}ms", expiration);

        // 비밀키 최소 길이 검증 (HMAC-SHA256: 256비트 = 32바이트)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) for HS256");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = expiration;
    }

    /**
     * JWT 토큰 생성
     */
    public String generateToken(Long userId, UserRole role) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰 생성 (기본 역할: USER) - 하위 호환성 유지
     */
    public String generateToken(Long userId) {
        return generateToken(userId, UserRole.USER);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰 파싱 에러: {}", e.getMessage());
            throw new UnauthorizedException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    /**
     * JWT 토큰에서 역할(Role) 추출
     */
    public UserRole getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String roleStr = claims.get("role", String.class);
            return roleStr != null ? UserRole.valueOf(roleStr) : UserRole.USER;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰에서 role 추출 실패: {}", e.getMessage());
            return UserRole.USER; // 기본값 반환
        }
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}