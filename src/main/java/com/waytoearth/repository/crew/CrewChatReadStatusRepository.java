package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewChatReadStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewChatReadStatusRepository extends JpaRepository<CrewChatReadStatusEntity, Long> {

    @Query("""
        SELECT r FROM CrewChatReadStatusEntity r
        WHERE r.message.id = :messageId AND r.reader.id = :userId
        """)
    Optional<CrewChatReadStatusEntity> findByMessageIdAndUserId(@Param("messageId") Long messageId,
                                                               @Param("userId") Long userId);

    @Query("""
        SELECT r FROM CrewChatReadStatusEntity r
        WHERE r.message.crew.id = :crewId AND r.reader.id = :userId
        ORDER BY r.readAt DESC
        """)
    List<CrewChatReadStatusEntity> findByCrewIdAndUserId(@Param("crewId") Long crewId,
                                                        @Param("userId") Long userId);

    @Query("""
        SELECT COUNT(r) FROM CrewChatReadStatusEntity r
        WHERE r.message.id = :messageId
        """)
    long countByMessageId(@Param("messageId") Long messageId);

    void deleteAllByMessage_Crew_Id(Long crewId);

    boolean existsByMessage_IdAndReader_Id(Long messageId, Long readerId);

    // 회원 탈퇴 시 사용 - 해당 유저의 읽음 상태 삭제
    @Modifying
    @Query("DELETE FROM CrewChatReadStatusEntity r WHERE r.reader.id = :userId")
    void deleteByReaderId(@Param("userId") Long userId);
}
