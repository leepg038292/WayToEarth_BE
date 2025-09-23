package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewChatEntity;
import com.waytoearth.entity.crew.CrewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrewChatRepository extends JpaRepository<CrewChatEntity, Long> {

    Page<CrewChatEntity> findByCrewAndIsActiveTrueOrderByCreatedAtDesc(CrewEntity crew, Pageable pageable);

    Optional<CrewChatEntity> findFirstByCrewAndIsActiveTrueOrderByCreatedAtDesc(CrewEntity crew);

    List<CrewChatEntity> findByCrewAndIsActiveTrueAndCreatedAtAfterOrderByCreatedAtAsc(
            CrewEntity crew, LocalDateTime after);

    //N+1 해결을 위한 발신자 정보 포함 조회
    @Query("SELECT cc FROM CrewChatEntity cc " +
           "JOIN FETCH cc.sender " +
           "WHERE cc.crew = :crew AND cc.isActive = true " +
           "ORDER BY cc.createdAt DESC")
    Page<CrewChatEntity> findRecentMessagesWithSender(@Param("crew") CrewEntity crew, Pageable pageable);

    //실시간 업데이트용 (발신자 정보 포함)
    @Query("SELECT cc FROM CrewChatEntity cc " +
           "JOIN FETCH cc.sender " +
           "WHERE cc.crew.id = :crewId AND cc.isActive = true " +
           "AND cc.createdAt > :timestamp " +
           "ORDER BY cc.createdAt ASC")
    List<CrewChatEntity> findMessagesAfter(@Param("crewId") Long crewId,
                                          @Param("timestamp") LocalDateTime timestamp);
}