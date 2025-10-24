package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewChatNotificationSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewChatNotificationSettingRepository extends JpaRepository<CrewChatNotificationSettingEntity, Long> {

    @Query("""
        SELECT n FROM CrewChatNotificationSettingEntity n
        WHERE n.crew.id = :crewId AND n.user.id = :userId
        """)
    Optional<CrewChatNotificationSettingEntity> findByCrewIdAndUserId(@Param("crewId") Long crewId,
                                                                     @Param("userId") Long userId);

    @Query("""
        SELECT n FROM CrewChatNotificationSettingEntity n
        WHERE n.user.id = :userId
        """)
    List<CrewChatNotificationSettingEntity> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT n FROM CrewChatNotificationSettingEntity n
        WHERE n.crew.id = :crewId
        """)
    List<CrewChatNotificationSettingEntity> findByCrewId(@Param("crewId") Long crewId);

    boolean existsByCrewIdAndUserId(Long crewId, Long userId);

    void deleteAllByCrew_Id(Long crewId);

    void deleteByCrewIdAndUserId(Long crewId, Long userId);

    @Modifying
    @Query("DELETE FROM CrewChatNotificationSettingEntity n WHERE n.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
