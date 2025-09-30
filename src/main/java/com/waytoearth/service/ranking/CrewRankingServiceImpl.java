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
        // TODO: DB에서 해당 월의 모든 랭킹 데이터를 조회하여 Redis에 저장
        log.info("월별 랭킹 데이터 재구축 완료. month: {}", month);
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