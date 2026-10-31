package aegis.server.domain.featureflag.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeaturePolicyResponse(
        boolean signupAllowed,
        boolean studyCreationAllowed,
        boolean studyEnrollmentAllowed,

        @Schema(description = "스터디 신청 시작 일시. 신청 기간이 설정되지 않았으면 null", nullable = true)
        LocalDateTime studyEnrollmentOpenAt,

        @Schema(description = "스터디 신청 종료 일시. 신청 기간이 설정되지 않았으면 null", nullable = true)
        LocalDateTime studyEnrollmentCloseAt) {

    public static FeaturePolicyResponse of(
            boolean signupAllowed,
            boolean studyCreationAllowed,
            boolean studyEnrollmentAllowed,
            LocalDateTime studyEnrollmentOpenAt,
            LocalDateTime studyEnrollmentCloseAt) {
        return new FeaturePolicyResponse(
                signupAllowed,
                studyCreationAllowed,
                studyEnrollmentAllowed,
                studyEnrollmentOpenAt,
                studyEnrollmentCloseAt);
    }
}
