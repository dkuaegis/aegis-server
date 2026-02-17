package aegis.server.domain.featureflag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.domain.FeatureFlagValueType;
import aegis.server.domain.featureflag.dto.request.StudyEnrollWindowUpdateRequest;
import aegis.server.domain.featureflag.dto.response.AdminFeatureFlagsResponse;
import aegis.server.domain.featureflag.dto.response.StudyEnrollWindowFlagResponse;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFeatureFlagService {

    private final FeatureFlagService featureFlagService;
    private final FeaturePolicyService featurePolicyService;

    public AdminFeatureFlagsResponse getFeatureFlags() {
        return buildAdminResponse();
    }

    @Transactional
    public AdminFeatureFlagsResponse updateStudyEnrollWindow(StudyEnrollWindowUpdateRequest request) {
        if (!request.closeAt().isAfter(request.openAt())) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        featureFlagService.upsertAll(List.of(
                FeatureFlagUpsertCommand.of(
                        FeatureFlagKey.STUDY_ENROLL_WINDOW_OPEN_AT,
                        FeatureFlagValueType.LOCAL_DATE_TIME,
                        request.openAt().toString(),
                        "스터디 신청 허용 시작 일시"),
                FeatureFlagUpsertCommand.of(
                        FeatureFlagKey.STUDY_ENROLL_WINDOW_CLOSE_AT,
                        FeatureFlagValueType.LOCAL_DATE_TIME,
                        request.closeAt().toString(),
                        "스터디 신청 허용 종료 일시")));

        return buildAdminResponse();
    }

    private AdminFeatureFlagsResponse buildAdminResponse() {
        FeaturePolicyService.StudyEnrollWindowEvaluation enrollWindow =
                featurePolicyService.evaluateStudyEnrollWindow();

        return AdminFeatureFlagsResponse.of(StudyEnrollWindowFlagResponse.of(
                enrollWindow.openFlagId(),
                enrollWindow.closeFlagId(),
                enrollWindow.openAtRaw(),
                enrollWindow.closeAtRaw(),
                enrollWindow.openAt(),
                enrollWindow.closeAt(),
                enrollWindow.valid(),
                enrollWindow.enrollmentAllowedNow()));
    }
}
