package com.waytoearth.service.file;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.file.PresignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private static final long MAX_PROFILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final long MAX_FEED_SIZE = 10L * 1024 * 1024;   // 10MB
    private static final long MAX_JOURNEY_SIZE = 10L * 1024 * 1024; // 10MB for journey images
    private static final Duration EXPIRES_IN = Duration.ofMinutes(5);

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region}")
    private String region;




    // 프로필 Presign 발급
    public PresignResponse presignProfile(Long userId, PresignRequest req) {
        validateProfile(userId, req);

        final String ext = switch (req.getContentType().toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };

        String key = String.format("profiles/%d/profile.%s", userId, ext);

        String uploadUrl = createPresignedPutUrl(key, req.getContentType());
        String downloadUrl = createPresignedGetUrl(key);

        log.info("[S3 Presign Profile] userId={}, key={}, uploadUrl={}, downloadUrl={}",
                userId, key, uploadUrl, downloadUrl);

        return new PresignResponse(uploadUrl, downloadUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    // 피드 Presign 발급
    public PresignResponse presignFeed(Long userId, PresignRequest req) {
        if (!req.getContentType().matches("^image/(jpeg|png|webp)$")) {
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        }
        if (req.getSize() > MAX_FEED_SIZE) {
            throw new IllegalArgumentException("파일 용량 초과 (최대 10MB)");
        }

        String key = String.format("feeds/%s/%s/%s", LocalDate.now(), userId, UUID.randomUUID());

        String uploadUrl = createPresignedPutUrl(key, req.getContentType());
        String downloadUrl = createPresignedGetUrl(key);

        log.info("[S3 Presign Feed] userId={}, key={}, uploadUrl={}, downloadUrl={}",
                userId, key, uploadUrl, downloadUrl);

        return new PresignResponse(uploadUrl, downloadUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    // 방명록 이미지 Presign 발급
    public PresignResponse presignGuestbook(Long userId, PresignRequest req) {
        validateJourneyImage(userId, req);

        String key = String.format("journeys/guestbooks/%s/%s/%s", LocalDate.now(), userId, UUID.randomUUID());

        String uploadUrl = createPresignedPutUrl(key, req.getContentType());
        String downloadUrl = createPresignedGetUrl(key);

        log.info("[S3 Presign Guestbook] userId={}, key={}, uploadUrl={}, downloadUrl={}",
                userId, key, uploadUrl, downloadUrl);

        return new PresignResponse(uploadUrl, downloadUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    // 스토리 이미지 Presign 발급
    public PresignResponse presignStory(Long userId, PresignRequest req) {
        validateJourneyImage(userId, req);

        String key = String.format("journeys/stories/%s/%s/%s", LocalDate.now(), userId, UUID.randomUUID());

        String uploadUrl = createPresignedPutUrl(key, req.getContentType());
        String downloadUrl = createPresignedGetUrl(key);

        log.info("[S3 Presign Story] userId={}, key={}, uploadUrl={}, downloadUrl={}",
                userId, key, uploadUrl, downloadUrl);

        return new PresignResponse(uploadUrl, downloadUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    // 랜드마크 이미지 Presign 발급
    public PresignResponse presignLandmark(Long userId, PresignRequest req) {
        validateJourneyImage(userId, req);

        String key = String.format("journeys/landmarks/%s/%s/%s", LocalDate.now(), userId, UUID.randomUUID());

        String uploadUrl = createPresignedPutUrl(key, req.getContentType());
        String downloadUrl = createPresignedGetUrl(key);

        log.info("[S3 Presign Landmark] userId={}, key={}, uploadUrl={}, downloadUrl={}",
                userId, key, uploadUrl, downloadUrl);

        return new PresignResponse(uploadUrl, downloadUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    // 공통 Presign PUT
    private String createPresignedPutUrl(String key, String contentType) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(EXPIRES_IN)
                .build();

        URL url = presigner.presignPutObject(presignReq).url();
        return url.toString();
    }

    // 공통 Presign GET
    public String createPresignedGetUrl(String key) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .getObjectRequest(get)
                .signatureDuration(EXPIRES_IN)
                .build();

        URL url = presigner.presignGetObject(presignReq).url();
        return url.toString();
    }

    // S3 삭제
    public void deleteObject(String key) {
        if (key == null || key.isBlank()) return;
        try (S3Client s3 = S3Client.builder().region(Region.of(region)).build()) {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("[S3 Delete] key={}", key);
        }
    }

    private void validateProfile(Long userId, PresignRequest req) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID");
        }
        if (req == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }
        if (req.getContentType() == null || !req.getContentType().matches("^image/(jpeg|png|webp)$")) {
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        }
        if (req.getSize() <= 0 || req.getSize() > MAX_PROFILE_SIZE) {
            throw new IllegalArgumentException("파일 크기 초과(최대 5MB)");
        }
    }

    private void validateJourneyImage(Long userId, PresignRequest req) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID");
        }
        if (req == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }
        if (req.getContentType() == null || !req.getContentType().matches("^image/(jpeg|png|webp)$")) {
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        }
        if (req.getSize() <= 0 || req.getSize() > MAX_JOURNEY_SIZE) {
            throw new IllegalArgumentException("파일 크기 초과(최대 10MB)");
        }
    }
}
