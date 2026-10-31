package aegis.server.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record ApplicantStudyApplicationStatus(
        @Schema(nullable = true) Long studyApplicationId, StudyApplicationStatus status) {
    public static ApplicantStudyApplicationStatus from(StudyApplication studyApplication) {
        return new ApplicantStudyApplicationStatus(studyApplication.getId(), studyApplication.getStatus());
    }

    public static ApplicantStudyApplicationStatus none() {
        return new ApplicantStudyApplicationStatus(null, StudyApplicationStatus.NONE);
    }
}
