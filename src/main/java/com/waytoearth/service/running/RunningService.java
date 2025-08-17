package com.waytoearth.service.running;

import com.waytoearth.dto.request.running.*;
import com.waytoearth.dto.response.running.*;
import com.waytoearth.security.AuthenticatedUser;

import java.util.List;

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
}