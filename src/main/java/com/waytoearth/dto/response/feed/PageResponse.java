package com.waytoearth.dto.response.feed;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    @Schema(description = "조회된 피드 목록")
    private List<T> feeds;

    @Schema(description = "다음 페이지 여부", example = "true")
    private boolean hasMore;
}
