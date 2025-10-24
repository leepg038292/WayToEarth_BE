package com.waytoearth.dto.response.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "커서 기반 페이징 응답")
public class CursorPageResponse<T> {

    @Schema(description = "데이터 목록")
    private List<T> content;

    @Schema(description = "다음 페이지 커서 (null이면 마지막 페이지)", example = "91")
    private Long nextCursor;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "현재 페이지 데이터 개수", example = "10")
    private int size;

    public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
        return new CursorPageResponse<>(content, nextCursor, hasNext, content.size());
    }
}
