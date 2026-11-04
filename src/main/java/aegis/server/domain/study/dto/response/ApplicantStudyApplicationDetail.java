package aegis.server.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record ApplicantStudyApplicationDetail(
        Long studyApplicationId,
        StudyApplicationStatus status,
        @Schema(nullable = true) String applicationReason,
        String studyTitle,
        String studyDescription) {
    public static ApplicantStudyApplicationDetail from(StudyApplication studyApplication) {
        return new ApplicantStudyApplicationDetail(
                studyApplication.getId(),
                studyApplication.getStatus(),
                studyApplication.getApplicationReason(),
                studyApplication.getStudy().getTitle(),
                studyApplication.getStudy().getDescription());
    }
}
