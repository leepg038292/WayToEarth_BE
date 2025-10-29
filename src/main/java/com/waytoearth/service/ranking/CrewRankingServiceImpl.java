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
    private final com.waytoearth.service.file.FileService fileService;

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
        // 모든 크루 목록 조회
        List<CrewEntity> crews = crewRepository.findAll();

        int rebuiltCount = 0;
        for (CrewEntity crew : crews) {
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

        // 프로필 이미지 URL 추가 (DB 조회 시에는 없으므로 추가)
        List<Long> userIds = ranking.stream()
            .map(CrewMemberRankingDto::getUserId)
            .toList();

        List<User> users = userRepository.findAllById(userIds);
        Map<Long, User> userMap = users.stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        // 프로필 이미지를 포함한 새로운 DTO 리스트 생성
        List<CrewMemberRankingDto> enrichedRanking = new ArrayList<>();
        for (CrewMemberRankingDto member : ranking) {
            User user = userMap.get(member.getUserId());
            String profileImageUrl = null;

            if (user != null) {
                if (user.getProfileImageKey() != null && !user.getProfileImageKey().isEmpty()) {
                    profileImageUrl = fileService.createPresignedGetUrl(user.getProfileImageKey());
                } else if (user.getProfileImageUrl() != null) {
                    profileImageUrl = user.getProfileImageUrl();
                }
            }

            enrichedRanking.add(new CrewMemberRankingDto(
                member.getMonth(),
                member.getUserId(),
                member.getUserName(),
                profileImageUrl,
                member.getTotalDistance(),
                member.getRunCount(),
                member.getRank()
            ));
        }

        log.info("DB에서 조회한 멤버 랭킹을 Redis에 캐싱했습니다. crewId: {}, month: {}", crewId, month);
        return enrichedRanking;
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

        // 프로필 이미지 URL 추가 (DB 조회 시에는 없으므로 추가)
        List<Long> crewIds = ranking.stream()
            .map(CrewRankingDto::getCrewId)
            .toList();

        List<CrewEntity> crews = crewRepository.findAllById(crewIds);
        Map<Long, CrewEntity> crewMap = crews.stream()
            .collect(java.util.stream.Collectors.toMap(CrewEntity::getId, crew -> crew));

        // 프로필 이미지를 포함한 새로운 DTO 리스트 생성
        List<CrewRankingDto> enrichedRanking = new ArrayList<>();
        for (CrewRankingDto crewDto : ranking) {
            CrewEntity crew = crewMap.get(crewDto.getCrewId());
            String profileImageUrl = null;

            if (crew != null) {
                if (crew.getProfileImageKey() != null && !crew.getProfileImageKey().isEmpty()) {
                    profileImageUrl = fileService.createPresignedGetUrl(crew.getProfileImageKey());
                } else if (crew.getProfileImageUrl() != null) {
                    profileImageUrl = crew.getProfileImageUrl();
                }
            }

            enrichedRanking.add(new CrewRankingDto(
                crewDto.getMonth(),
                crewDto.getCrewId(),
                crewDto.getCrewName(),
                profileImageUrl,
                crewDto.getTotalDistance(),
                crewDto.getRunCount(),
                crewDto.getRank()
            ));
        }

        log.info("DB에서 조회한 크루 랭킹을 Redis에 캐싱했습니다. month: {}", month);
        return enrichedRanking;
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
        Map<Long, User> userMap = users.stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        // Redis에서 러닝 횟수 조회
        Map<Long, Integer> runCountMap = new HashMap<>();
        for (Long userId : userIds) {
            runCountMap.put(userId, getMemberRunCount(crewId, userId, month));
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : rankingSet) {
            Long userId = Long.valueOf(tuple.getValue().toString());
            Double totalDistance = tuple.getScore();

            User user = userMap.get(userId);
            if (user != null) {
                // profileImageKey가 있으면 CloudFront URL 생성, 없으면 기존 URL 사용
                String profileImageUrl = null;
                if (user.getProfileImageKey() != null && !user.getProfileImageKey().isEmpty()) {
                    profileImageUrl = fileService.createPresignedGetUrl(user.getProfileImageKey());
                } else if (user.getProfileImageUrl() != null) {
                    profileImageUrl = user.getProfileImageUrl();
                }

                ranking.add(new CrewMemberRankingDto(
                    month,
                    userId,
                    user.getNickname(),
                    profileImageUrl,
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
        Map<Long, CrewEntity> crewMap = crews.stream()
            .collect(java.util.stream.Collectors.toMap(CrewEntity::getId, crew -> crew));

        // Redis에서 러닝 횟수 조회
        Map<Long, Integer> runCountMap = new HashMap<>();
        for (Long crewId : crewIds) {
            runCountMap.put(crewId, getCrewRunCount(crewId, month));
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : rankingSet) {
            Long crewId = Long.valueOf(tuple.getValue().toString());
            Double totalDistance = tuple.getScore();

            CrewEntity crew = crewMap.get(crewId);
            if (crew != null) {
                // profileImageKey가 있으면 CloudFront URL 생성, 없으면 기존 URL 사용
                String profileImageUrl = null;
                if (crew.getProfileImageKey() != null && !crew.getProfileImageKey().isEmpty()) {
                    profileImageUrl = fileService.createPresignedGetUrl(crew.getProfileImageKey());
                } else if (crew.getProfileImageUrl() != null) {
                    profileImageUrl = crew.getProfileImageUrl();
                }

                ranking.add(new CrewRankingDto(
                    month,
                    crewId,
                    crew.getName(),
                    profileImageUrl,
                    BigDecimal.valueOf(totalDistance),
                    runCountMap.getOrDefault(crewId, 0),
                    rank++
                ));
            } else {
                // 크루가 삭제된 경우 (Redis에는 남아있지만 DB에는 없음)
                log.warn("크루 랭킹에 삭제된 크루가 포함되어 있습니다. crewId: {}, month: {}", crewId, month);
                // 삭제된 크루는 랭킹에서 제외
            }
        }

        return ranking;
    }

    @Override
    public void removeCrewFromAllRankings(Long crewId) {
        // 현재 달부터 과거 12개월 치 랭킹에서 크루 제거
        java.time.YearMonth currentMonth = java.time.YearMonth.now();

        for (int i = 0; i < 12; i++) {
            java.time.YearMonth targetMonth = currentMonth.minusMonths(i);
            String month = targetMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));

            String rankingKey = RankingKeyUtil.crewRankingKey(month);
            String runCountKey = RankingKeyUtil.crewRunCountKey(month);

            // Redis ZSet에서 제거
            redisTemplate.opsForZSet().remove(rankingKey, crewId.toString());

            // Redis Hash에서 러닝 횟수 제거
            redisTemplate.opsForHash().delete(runCountKey, crewId.toString());
        }

        log.info("크루 랭킹 데이터가 Redis에서 삭제되었습니다. crewId: {}", crewId);
    }
}