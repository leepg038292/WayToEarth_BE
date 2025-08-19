// com/waytoearth/service/file/FileService.java
package com.waytoearth.service.file;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.file.PresignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Duration EXPIRES_IN = Duration.ofMinutes(5);

    private final S3Presigner presigner;

    @Value("${cloud.aws.s3.bucket}") private String bucket;
    @Value("${cloud.aws.region}")    private String region;

    public PresignResponse presignProfile(Long userId, PresignRequest req) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID");
        }
        if (req == null) {
            throw new IllegalArgumentException("요청이 비어 있습니다.");
        }

        final String contentType = safeLower(req.getContentType());
        final long size = req.getSize();

        // 1) 타입/사이즈 검증
        if (contentType == null || !contentType.matches("^image/(jpeg|png|webp)$")) {
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException("파일 크기 초과(최대 5MB)");
        }

        // 2) 확장자는 contentType 기준으로 신뢰 (fileName의 확장자는 참고하지 않음)
        final String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };

        // 3) 오브젝트 키 생성 (연/월/일/유저ID/UUID.ext)
        LocalDate today = LocalDate.now();
        String key = String.format(
                "profiles/%d/%02d/%02d/%d/%s.%s",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth(),
                userId, UUID.randomUUID(), ext
        );

        // 4) PutObject 요청 및 프리사인
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(EXPIRES_IN)
                .build();

        URL signedUrl = presigner.presignPutObject(presignReq).url();

        // 5) 퍼블릭 접근 URL(버킷 퍼블릭/정책에 따라 접근 가능 여부 달라짐)
        String publicUrl = String.format(
                Locale.ROOT,
                "https://%s.s3.%s.amazonaws.com/%s",
                bucket, region, key
        );

        log.info("[S3 Presign] userId={}, key={}, contentType={}, size={}", userId, key, contentType, size);
        return new PresignResponse(signedUrl.toString(), publicUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    private static String safeLower(String v) {
        return v == null ? null : v.toLowerCase(Locale.ROOT).trim();
    }

    // com/waytoearth/service/file/FileService.java

    public PresignResponse presignFeed(Long userId, PresignRequest req) {
        if (!req.getContentType().matches("^image/(jpeg|png|webp)$"))
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        if (req.getSize() > 5 * 1024 * 1024) // 피드 이미지라면 용량 제한 ↑
            throw new IllegalArgumentException("파일 용량 초과 (최대 10MB)");

        String key = String.format(
                "feeds/%s/%s/%s",
                LocalDate.now(),
                userId,
                UUID.randomUUID()
        );

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        URL url = presigner.presignPutObject(presignRequest).url();

        return new PresignResponse(
                url.toString(),
                String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key),
                key,
                300  // Presigned URL 만료 시간 (예: 300초)
        );
    }

}
