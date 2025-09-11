package com.waytoearth.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이지네이션 응답 데이터")
public class PagedResponse<T> {

    @Schema(description = "데이터 목록")
    private final List<T> content;

    @Schema(description = "페이지 정보")
    private final PageInfo page;

    @Getter
    @AllArgsConstructor
    @Schema(description = "페이지 정보")
    public static class PageInfo {
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private final int number;

        @Schema(description = "페이지 크기", example = "10")
        private final int size;

        @Schema(description = "전체 요소 수", example = "100")
        private final long totalElements;

        @Schema(description = "전체 페이지 수", example = "10")
        private final int totalPages;

        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private final boolean first;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private final boolean last;

        @Schema(description = "비어있는 페이지 여부", example = "false")
        private final boolean empty;
    }

    public static <T> PagedResponse<T> of(List<T> content, Page<?> page) {
        PageInfo pageInfo = new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
        return new PagedResponse<>(content, pageInfo);
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return of(page.getContent(), page);
    }
}