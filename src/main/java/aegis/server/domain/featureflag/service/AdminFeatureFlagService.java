package aegis.server.domain.featureflag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.domain.FeatureFlagKey;
import aegis.server.domain.featureflag.domain.FeatureFlagValueType;
import aegis.server.domain.featureflag.dto.request.MemberSignupUpdateRequest;
import aegis.server.domain.featureflag.dto.request.StudyCreationUpdateRequest;
import aegis.server.domain.featureflag.dto.request.StudyEnrollWindowUpdateRequest;
import aegis.server.domain.featureflag.dto.response.AdminFeatureFlagsResponse;
import aegis.server.domain.featureflag.dto.response.MemberSignupFlagResponse;
import aegis.server.domain.featureflag.dto.response.StudyCreationFlagResponse;
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
    public AdminFeatureFlagsResponse updateMemberSignup(MemberSignupUpdateRequest request) {
        featureFlagService.upsertAll(List.of(FeatureFlagUpsertCommand.of(
                FeatureFlagKey.MEMBER_SIGNUP_ENABLED,
                FeatureFlagValueType.BOOLEAN,
                request.enabled().toString(),
                "회원가입 허용 여부")));

        return buildAdminResponse();
    }

    @Transactional
    public AdminFeatureFlagsResponse updateStudyCreation(StudyCreationUpdateRequest request) {
        featureFlagService.upsertAll(List.of(FeatureFlagUpsertCommand.of(
                FeatureFlagKey.STUDY_CREATION_ENABLED,
                FeatureFlagValueType.BOOLEAN,
                request.enabled().toString(),
                "스터디 개설 API 허용 여부")));

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
        FeaturePolicyService.MemberSignupEvaluation memberSignup = featurePolicyService.evaluateMemberSignup();
        FeaturePolicyService.StudyCreationEvaluation studyCreation = featurePolicyService.evaluateStudyCreation();
        FeaturePolicyService.StudyEnrollWindowEvaluation enrollWindow =
                featurePolicyService.evaluateStudyEnrollWindow();

        return AdminFeatureFlagsResponse.of(
                MemberSignupFlagResponse.of(
                        memberSignup.featureFlagId(),
                        memberSignup.rawValue(),
                        memberSignup.enabled(),
                        memberSignup.valid(),
                        memberSignup.signupAllowed()),
                StudyCreationFlagResponse.of(
                        studyCreation.featureFlagId(),
                        studyCreation.rawValue(),
                        studyCreation.enabled(),
                        studyCreation.valid(),
                        studyCreation.studyCreationAllowed()),
                StudyEnrollWindowFlagResponse.of(
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
