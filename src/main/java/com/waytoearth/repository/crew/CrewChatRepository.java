package com.waytoearth.repository.crew;

import com.waytoearth.dto.response.crew.CrewChatMessageDto;
import com.waytoearth.entity.crew.CrewChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.waytoearth.entity.user.User;

@Repository
public interface CrewChatRepository extends JpaRepository<CrewChatEntity, Long> {

    @Query("""
        SELECT c FROM CrewChatEntity c
        JOIN FETCH c.sender
        WHERE c.crew.id = :crewId
        AND c.isDeleted = false
        ORDER BY c.sentAt DESC
        """)
    Page<CrewChatEntity> findChatEntitiesWithReadStatus(@Param("crewId") Long crewId,
                                                        @Param("userId") Long userId,
                                                        Pageable pageable);

    @Query("""
        SELECT c FROM CrewChatEntity c
        WHERE c.crew.id = :crewId
        AND c.id > :afterMessageId
        AND c.isDeleted = false
        AND c.sender.id != :userId
        AND NOT EXISTS(SELECT 1 FROM CrewChatReadStatusEntity r
                      WHERE r.message = c AND r.reader.id = :userId)
        ORDER BY c.sentAt ASC
        """)
    List<CrewChatEntity> findUnreadMessagesAfter(@Param("crewId") Long crewId,
                                                 @Param("userId") Long userId,
                                                 @Param("afterMessageId") Long afterMessageId);

    @Query("""
        SELECT COUNT(c)
        FROM CrewChatEntity c
        WHERE c.crew.id = :crewId
        AND c.isDeleted = false
        AND c.sender.id != :userId
        AND NOT EXISTS(SELECT 1 FROM CrewChatReadStatusEntity r
                      WHERE r.message = c AND r.reader.id = :userId)
        """)
    int countUnreadMessages(@Param("crewId") Long crewId, @Param("userId") Long userId);

    @Query("""
        SELECT c FROM CrewChatEntity c
        JOIN FETCH c.sender
        WHERE c.crew.id = :crewId
        AND c.isDeleted = false
        ORDER BY c.sentAt DESC
        """)
    List<CrewChatEntity> findRecentChatEntities(@Param("crewId") Long crewId, Pageable pageable);

    @Query("""
        SELECT c FROM CrewChatEntity c
        WHERE c.crew.id = :crewId
        AND c.isDeleted = false
        ORDER BY c.sentAt DESC
        """)
    List<CrewChatEntity> findByCrewIdOrderBySentAtDesc(@Param("crewId") Long crewId);

    void deleteAllByCrew_Id(Long crewId);

    @Modifying
    @Query("UPDATE CrewChatEntity c SET c.sender = :sentinel WHERE c.sender.id = :userId")
    int reassignSenderToDeleted(@Param("userId") Long userId, @Param("sentinel") User sentinel);
}
