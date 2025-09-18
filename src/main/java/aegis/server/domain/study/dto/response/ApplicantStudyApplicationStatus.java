package aegis.server.domain.study.dto.response;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record ApplicantStudyApplicationStatus(Long studyApplicationId, StudyApplicationStatus status) {
    public static ApplicantStudyApplicationStatus from(StudyApplication studyApplication) {
        return new ApplicantStudyApplicationStatus(studyApplication.getId(), studyApplication.getStatus());
    }

    public static ApplicantStudyApplicationStatus none() {
        return new ApplicantStudyApplicationStatus(null, StudyApplicationStatus.NONE);
    }
}
