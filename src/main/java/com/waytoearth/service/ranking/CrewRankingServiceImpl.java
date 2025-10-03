package com.waytoearth.service.ranking;

import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewStatisticsRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.util.RankingKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrewRankingServiceImpl implements CrewRankingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CrewStatisticsRepository statisticsRepository;
    private final UserRepository userRepository;
    private final CrewRepository crewRepository;

    @Override
    public List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit) {
        String rankingKey = RankingKeyUtil.memberRankingKey(crewId, month);

        // Redis ZSet에서 상위 랭킹 조회
        Set<ZSetOperations.TypedTuple<Object>> rankingSet =
            redisTemplate.opsForZSet().reverseRangeWithScores(rankingKey, 0, limit - 1);

        if (rankingSet == null || rankingSet.isEmpty()) {
            log.info("Redis에 랭킹 데이터가 없어 DB에서 조회합니다. crewId: {}, month: {}", crewId, month);
            return getMemberRankingFromDB(crewId, month, limit);
        }

        return convertToMemberRankingDto(crewId, rankingSet, month);
    }

    @Override
    public List<CrewRankingDto> getCrewRanking(String month, int limit) {
        String rankingKey = RankingKeyUtil.crewRankingKey(month);

        Set<ZSetOperations.TypedTuple<Object>> rankingSet =
            redisTemplate.opsForZSet().reverseRangeWithScores(rankingKey, 0, limit - 1);

        if (rankingSet == null || rankingSet.isEmpty()) {
            log.info("Redis에 크루 랭킹 데이터가 없어 DB에서 조회합니다. month: {}", month);
            return getCrewRankingFromDB(month, limit);
        }

        return convertToCrewRankingDto(rankingSet, month);
    }

    @Override
    public void updateMemberRanking(Long crewId, Long userId, String month, Double newTotalDistance) {
        String rankingKey = RankingKeyUtil.memberRankingKey(crewId, month);
        redisTemplate.opsForZSet().add(rankingKey, userId.toString(), newTotalDistance);
        log.debug("멤버 랭킹 업데이트 완료. crewId: {}, userId: {}, month: {}, distance: {}",
                crewId, userId, month, newTotalDistance);
    }

    @Override
    public void updateCrewRanking(Long crewId, String month, Double newTotalDistance) {
        String rankingKey = RankingKeyUtil.crewRankingKey(month);
        redisTemplate.opsForZSet().add(rankingKey, crewId.toString(), newTotalDistance);
        log.debug("크루 랭킹 업데이트 완료. crewId: {}, month: {}, distance: {}",
                crewId, month, newTotalDistance);
    }

    @Override
    public void incrementMemberRunCount(Long crewId, Long userId, String month) {
        String runCountKey = RankingKeyUtil.memberRunCountKey(crewId, month);
        redisTemplate.opsForHash().increment(runCountKey, userId.toString(), 1);
        log.debug("멤버 러닝 횟수 증가. crewId: {}, userId: {}, month: {}", crewId, userId, month);
    }

    @Override
    public void incrementCrewRunCount(Long crewId, String month) {
        String runCountKey = RankingKeyUtil.crewRunCountKey(month);
        redisTemplate.opsForHash().increment(runCountKey, crewId.toString(), 1);
        log.debug("크루 러닝 횟수 증가. crewId: {}, month: {}", crewId, month);
    }

    @Override
    public Integer getMemberRunCount(Long crewId, Long userId, String month) {
        String runCountKey = RankingKeyUtil.memberRunCountKey(crewId, month);
        Object count = redisTemplate.opsForHash().get(runCountKey, userId.toString());
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }

    @Override
    public Integer getCrewRunCount(Long crewId, String month) {
        String runCountKey = RankingKeyUtil.crewRunCountKey(month);
        Object count = redisTemplate.opsForHash().get(runCountKey, crewId.toString());
        return count != null ? Integer.valueOf(count.toString()) : 0;
    }

    @Override
    public void rebuildRankingFromDB(String month) {
        log.info("월별 랭킹 데이터를 DB에서 재구축 시작. month: {}", month);

        try {
            // 1. 크루 랭킹 재구축
            rebuildCrewRanking(month);

            // 2. 각 크루의 멤버 랭킹 재구축
            rebuildAllCrewMemberRankings(month);

            log.info("월별 랭킹 데이터 재구축 완료. month: {}", month);
        } catch (Exception e) {
            log.error("랭킹 데이터 재구축 중 오류 발생. month: {}, error: {}", month, e.getMessage(), e);
            throw new RuntimeException("랭킹 데이터 재구축 실패", e);
        }
    }

    /**
     * 크루 랭킹 재구축
     */
    private void rebuildCrewRanking(String month) {
        String rankingKey = RankingKeyUtil.crewRankingKey(month);
        String runCountKey = RankingKeyUtil.crewRunCountKey(month);

        // 기존 Redis 데이터 삭제
        redisTemplate.delete(rankingKey);
        redisTemplate.delete(runCountKey);

        // DB에서 모든 크루 랭킹 조회 (limit 없이 전체 조회)
        List<CrewRankingDto> allCrewRankings = statisticsRepository.findCrewRankingByActualDistance(month, 1000);

        if (allCrewRankings.isEmpty()) {
            log.warn("DB에 해당 월의 크루 랭킹 데이터가 없습니다. month: {}", month);
            return;
        }

        // Redis에 일괄 저장
        for (CrewRankingDto crew : allCrewRankings) {
            redisTemplate.opsForZSet().add(rankingKey,
                    crew.getCrewId().toString(),
                    crew.getTotalDistance().doubleValue());

            redisTemplate.opsForHash().put(runCountKey,
                    crew.getCrewId().toString(),
                    crew.getRunCount());
        }

        log.info("크루 랭킹 재구축 완료. month: {}, count: {}", month, allCrewRankings.size());
    }

    /**
     * 모든 크루의 멤버 랭킹 재구축
     */
    private void rebuildAllCrewMemberRankings(String month) {
        // 활성 크루 목록 조회
        List<CrewEntity> activeCrews = crewRepository.findAll().stream()
                .filter(CrewEntity::getIsActive)
                .toList();

        int rebuiltCount = 0;
        for (CrewEntity crew : activeCrews) {
            rebuildCrewMemberRanking(crew.getId(), month);
            rebuiltCount++;
        }

        log.info("전체 크루 멤버 랭킹 재구축 완료. month: {}, crewCount: {}", month, rebuiltCount);
    }

    /**
     * 특정 크루의 멤버 랭킹 재구축
     */
    private void rebuildCrewMemberRanking(Long crewId, String month) {
        String rankingKey = RankingKeyUtil.memberRankingKey(crewId, month);
        String runCountKey = RankingKeyUtil.memberRunCountKey(crewId, month);

        // 기존 Redis 데이터 삭제
        redisTemplate.delete(rankingKey);
        redisTemplate.delete(runCountKey);

        // DB에서 해당 크루의 모든 멤버 랭킹 조회
        List<CrewMemberRankingDto> memberRankings = statisticsRepository.findMemberRankingInCrew(crewId, month, 1000);

        if (memberRankings.isEmpty()) {
            return;
        }

        // Redis에 일괄 저장
        for (CrewMemberRankingDto member : memberRankings) {
            redisTemplate.opsForZSet().add(rankingKey,
                    member.getUserId().toString(),
                    member.getTotalDistance().doubleValue());

            redisTemplate.opsForHash().put(runCountKey,
                    member.getUserId().toString(),
                    member.getRunCount());
        }

        log.debug("크루 멤버 랭킹 재구축 완료. crewId: {}, month: {}, memberCount: {}",
                crewId, month, memberRankings.size());
    }

    private List<CrewMemberRankingDto> getMemberRankingFromDB(Long crewId, String month, int limit) {
        List<CrewMemberRankingDto> ranking = statisticsRepository.findMemberRankingInCrew(crewId, month, limit);

        // Redis에 캐싱
        String rankingKey = RankingKeyUtil.memberRankingKey(crewId, month);
        for (CrewMemberRankingDto member : ranking) {
            redisTemplate.opsForZSet().add(rankingKey,
                member.getUserId().toString(),
                member.getTotalDistance().doubleValue());
        }

        log.info("DB에서 조회한 멤버 랭킹을 Redis에 캐싱했습니다. crewId: {}, month: {}", crewId, month);
        return ranking;
    }

    private List<CrewRankingDto> getCrewRankingFromDB(String month, int limit) {
        List<CrewRankingDto> ranking = statisticsRepository.findCrewRankingByActualDistance(month, limit);

        // Redis에 캐싱
        String rankingKey = RankingKeyUtil.crewRankingKey(month);
        for (CrewRankingDto crew : ranking) {
            redisTemplate.opsForZSet().add(rankingKey,
                crew.getCrewId().toString(),
                crew.getTotalDistance().doubleValue());
        }

        log.info("DB에서 조회한 크루 랭킹을 Redis에 캐싱했습니다. month: {}", month);
        return ranking;
    }

    private List<CrewMemberRankingDto> convertToMemberRankingDto(Long crewId, Set<ZSetOperations.TypedTuple<Object>> rankingSet, String month) {
        List<CrewMemberRankingDto> ranking = new ArrayList<>();

        if (rankingSet == null || rankingSet.isEmpty()) {
            return ranking;
        }

        // N+1 문제 해결: 모든 userId를 한번에 조회
        List<Long> userIds = rankingSet.stream()
            .map(tuple -> Long.valueOf(tuple.getValue().toString()))
            .toList();

        List<User> users = userRepository.findAllById(userIds);
        Map<Long, String> userNicknameMap = users.stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, User::getNickname));

        // Redis에서 러닝 횟수 조회
        Map<Long, Integer> runCountMap = new HashMap<>();
        for (Long userId : userIds) {
            runCountMap.put(userId, getMemberRunCount(crewId, userId, month));
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : rankingSet) {
            Long userId = Long.valueOf(tuple.getValue().toString());
            Double totalDistance = tuple.getScore();

            String nickname = userNicknameMap.get(userId);
            if (nickname != null) {
                ranking.add(new CrewMemberRankingDto(
                    month,
                    userId,
                    nickname,
                    BigDecimal.valueOf(totalDistance),
                    runCountMap.getOrDefault(userId, 0),
                    rank++
                ));
            }
        }

        return ranking;
    }

    private List<CrewRankingDto> convertToCrewRankingDto(Set<ZSetOperations.TypedTuple<Object>> rankingSet, String month) {
        List<CrewRankingDto> ranking = new ArrayList<>();

        if (rankingSet == null || rankingSet.isEmpty()) {
            return ranking;
        }

        // N+1 문제 해결: 모든 crewId를 한번에 조회
        List<Long> crewIds = rankingSet.stream()
            .map(tuple -> Long.valueOf(tuple.getValue().toString()))
            .toList();

        List<CrewEntity> crews = crewRepository.findAllById(crewIds);
        Map<Long, String> crewNameMap = crews.stream()
            .collect(java.util.stream.Collectors.toMap(CrewEntity::getId, CrewEntity::getName));

        // Redis에서 러닝 횟수 조회
        Map<Long, Integer> runCountMap = new HashMap<>();
        for (Long crewId : crewIds) {
            runCountMap.put(crewId, getCrewRunCount(crewId, month));
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : rankingSet) {
            Long crewId = Long.valueOf(tuple.getValue().toString());
            Double totalDistance = tuple.getScore();

            String crewName = crewNameMap.getOrDefault(crewId, "알 수 없는 크루");
            ranking.add(new CrewRankingDto(
                month,
                crewId,
                crewName,
                BigDecimal.valueOf(totalDistance),
                runCountMap.getOrDefault(crewId, 0),
                rank++
            ));
        }

        return ranking;
    }
}