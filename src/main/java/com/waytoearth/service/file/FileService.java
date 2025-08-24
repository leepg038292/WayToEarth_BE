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

    //  프로필 이미지 Presign
    public PresignResponse presignProfile(Long userId, PresignRequest req) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("유효하지 않은 사용자 ID");
        if (req == null) throw new IllegalArgumentException("요청이 비어 있습니다.");

        final String contentType = safeLower(req.getContentType());
        final long size = req.getSize();

        if (contentType == null || !contentType.matches("^image/(jpeg|png|webp)$"))
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        if (size <= 0 || size > MAX_SIZE)
            throw new IllegalArgumentException("파일 크기 초과(최대 5MB)");

        final String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };

        //  항상 userId 기반 고정 key 사용 → 중복 방지
        String key = String.format("profiles/%d/profile.%s", userId, ext);

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
//                .contentLength(size)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(EXPIRES_IN)
                .build();

        URL signedUrl = presigner.presignPutObject(presignReq).url();

        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        log.info("[S3 Presign Profile] bucket={}, region={}, userId={}, key={}, uploadUrl={}, publicUrl={}",
                bucket, region, userId, key, signedUrl, publicUrl);

        return new PresignResponse(signedUrl.toString(), publicUrl, key, (int) EXPIRES_IN.getSeconds());
    }

    //  피드 이미지 Presign
    public PresignResponse presignFeed(Long userId, PresignRequest req) {
        if (!req.getContentType().matches("^image/(jpeg|png|webp)$"))
            throw new IllegalArgumentException("허용되지 않는 Content-Type");
        if (req.getSize() > 10 * 1024 * 1024)
            throw new IllegalArgumentException("파일 용량 초과 (최대 10MB)");

        String key = String.format("feeds/%s/%s/%s", LocalDate.now(), userId, UUID.randomUUID());

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(EXPIRES_IN)
                .putObjectRequest(objectRequest)
                .build();

        URL url = presigner.presignPutObject(presignRequest).url();

        return new PresignResponse(
                url.toString(),
                String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key),
                key,
                (int) EXPIRES_IN.getSeconds()
        );
    }

    //  S3 삭제
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

    private static String safeLower(String v) {
        return v == null ? null : v.toLowerCase(Locale.ROOT).trim();
    }
}
