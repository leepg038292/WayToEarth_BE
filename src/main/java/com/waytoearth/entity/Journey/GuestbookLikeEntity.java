package com.waytoearth.entity.Journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.User.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guestbook_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "guestbook_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방명록 좋아요 엔티티")
public class GuestbookLikeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "좋아요 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guestbook_id", nullable = false)
    @Schema(description = "방명록")
    private GuestbookEntity guestbook;
}