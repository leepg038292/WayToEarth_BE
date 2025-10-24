package com.waytoearth.repository.notification;

import com.waytoearth.entity.notification.NotificationSetting;
import com.waytoearth.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    /** 사용자의 알림 설정 조회 */
    Optional<NotificationSetting> findByUser(User user);

    /** 사용자 ID로 알림 설정 조회 */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.user.id = :userId")
    Optional<NotificationSetting> findByUserId(@Param("userId") Long userId);

    /** 사용자에게 알림 설정이 있는지 확인 */
    boolean existsByUser(User user);

    /**
     * 사용자 ID로 알림 설정 삭제 (회원 탈퇴용)
     */
    void deleteByUserId(Long userId);
}
