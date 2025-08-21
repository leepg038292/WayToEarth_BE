// com/waytoearth/repository/UserEmblemRepository.java
package com.waytoearth.repository;

import com.waytoearth.entity.Emblem;
import com.waytoearth.entity.User;
import com.waytoearth.entity.UserEmblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEmblemRepository extends JpaRepository<UserEmblem, Long> {

    /** 사용자 보유 엠블럼 개수 */
    @Query("select count(ue) from UserEmblem ue where ue.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /** 사용자 보유 엠블럼 목록 */
    @Query("select ue from UserEmblem ue join fetch ue.emblem where ue.user.id = :userId")
    List<UserEmblem> findByUserId(@Param("userId") Long userId);

    /** 특정 엠블럼 보유 여부 (userId, emblemId) */
    @Query("select case when count(ue) > 0 then true else false end " +
            "from UserEmblem ue where ue.user.id = :userId and ue.emblem.id = :emblemId")
    boolean existsByUserIdAndEmblemId(@Param("userId") Long userId, @Param("emblemId") Long emblemId);

    /** 특정 엠블럼 보유 레코드 (userId, emblemId) */
    @Query("select ue from UserEmblem ue " +
            "where ue.user.id = :userId and ue.emblem.id = :emblemId")
    Optional<UserEmblem> findByUserIdAndEmblemId(@Param("userId") Long userId, @Param("emblemId") Long emblemId);

    /** 사용자 보유 여부 (엔티티 직접 비교) - 서비스에서 사용 */
    boolean existsByUserAndEmblem(User user, Emblem emblem);

    /** 배치 조회: 주어진 emblemIds 중 보유한 목록 */
    @Query("select ue from UserEmblem ue " +
            "where ue.user.id = :userId and ue.emblem.id in :emblemIds")
    List<UserEmblem> findByUserIdAndEmblemIdIn(@Param("userId") Long userId,
                                               @Param("emblemIds") List<Long> emblemIds);
}
