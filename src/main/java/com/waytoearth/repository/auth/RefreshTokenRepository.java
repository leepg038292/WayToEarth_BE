package com.waytoearth.repository.auth;

import com.waytoearth.entity.auth.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RefreshToken Redis Repository
 * - CrudRepository를 상속받아 기본 CRUD 기능 제공
 * - Redis에 저장된 리프레시 토큰 관리
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    /**
     * 토큰 값으로 RefreshToken 조회
     * @param token 리프레시 토큰 값
     * @return RefreshToken Optional
     */
    Optional<RefreshToken> findByToken(String token);
}
