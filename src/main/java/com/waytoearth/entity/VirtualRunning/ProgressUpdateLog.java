package com.waytoearth.entity.VirtualRunning;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_update_log",
       indexes = {
           @Index(name = "idx_session_segment_distance", 
                  columnList = "session_id,segment_id,distance_km,created_at"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressUpdateLog {

    @Id
    private String id; // sessionId + "_" + segmentId + "_" + distanceKm + "_" + timestamp

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "segment_id", nullable = false)
    private Long segmentId;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static String generateId(String sessionId, Long segmentId, Double distanceKm) {
        return sessionId + "_" + segmentId + "_" + distanceKm + "_" + System.currentTimeMillis();
    }
}
