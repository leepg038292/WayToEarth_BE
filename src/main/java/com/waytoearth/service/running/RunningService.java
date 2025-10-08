package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.*;
import com.waytoearth.dto.response.common.CursorPageResponse;
import com.waytoearth.dto.response.running.RunningCompleteResponse;
import com.waytoearth.dto.response.running.RunningRecordSummaryResponse;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RunningService {

    RunningStartResponse startRunning(AuthenticatedUser user, RunningStartRequest request);

    void updateRunning(AuthenticatedUser user, RunningUpdateRequest request);

    //  공용 DTO로 통일
    void pauseRunning(AuthenticatedUser user, RunningPauseResumeRequest request);

    void resumeRunning(AuthenticatedUser user, RunningPauseResumeRequest request);

    RunningCompleteResponse completeRunning(AuthenticatedUser user, RunningCompleteRequest request);

    // 제목 수정
    void updateTitle(AuthenticatedUser user, Long recordId, RunningTitleUpdateRequest request);

    // 완료 페이지 재조회(경로 포함)
    RunningCompleteResponse getDetail(AuthenticatedUser user, Long recordId);

    Page<RunningRecordSummaryResponse> getRecords(AuthenticatedUser authUser, Pageable pageable);

    // 커서 기반 페이징
    CursorPageResponse<RunningRecordSummaryResponse> getRecordsByCursor(
            AuthenticatedUser user, Long cursor, int size);
}