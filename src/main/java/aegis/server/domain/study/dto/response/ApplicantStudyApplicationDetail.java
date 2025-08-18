package aegis.server.domain.study.dto.response;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record ApplicantStudyApplicationDetail(
        Long studyApplicationId,
        StudyApplicationStatus status,
        String applicationReason,
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
