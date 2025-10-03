package com.waytoearth.repository.user;

import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    boolean existsByNickname(String nickname);

    @Modifying
    @Query("UPDATE User u SET u.totalDistance = u.totalDistance + :distance, u.totalRunningCount = u.totalRunningCount + 1 WHERE u.id = :userId")
    int updateRunningStatsAtomic(@Param("userId") Long userId, @Param("distance") BigDecimal distance);
}