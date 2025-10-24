package com.waytoearth.repository.notification;

import com.waytoearth.entity.notification.FcmToken;
import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /** 사용자의 활성 토큰 조회 */
    List<FcmToken> findByUserAndIsActiveTrue(User user);

    /** 사용자 ID로 활성 토큰 조회 */
    @Query("SELECT f FROM FcmToken f WHERE f.user.id = :userId AND f.isActive = true")
    List<FcmToken> findActiveTokensByUserId(@Param("userId") Long userId);

    /** 전체 활성 토큰 조회 (정기 알림용) */
    @Query("SELECT f FROM FcmToken f WHERE f.isActive = true")
    List<FcmToken> findAllActiveTokens();

    /** 사용자 + 디바이스로 토큰 조회 */
    Optional<FcmToken> findByUserAndDeviceId(User user, String deviceId);

    /** FCM 토큰 문자열로 조회 */
    Optional<FcmToken> findByFcmToken(String fcmToken);

    /** 토큰 존재 여부 확인 */
    boolean existsByUserAndDeviceId(User user, String deviceId);

    /** 토큰 비활성화 */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.fcmToken = :token")
    int deactivateToken(@Param("token") String token);

    /** 사용자의 모든 토큰 비활성화 */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.user.id = :userId")
    int deactivateAllUserTokens(@Param("userId") Long userId);

    /** 특정 디바이스 토큰 비활성화 */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.user.id = :userId AND f.deviceId = :deviceId")
    int deactivateUserDeviceToken(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    /**
     * 사용자 ID로 FCM 토큰 일괄 삭제 (회원 탈퇴용)
     */
    void deleteByUserId(Long userId);
}
