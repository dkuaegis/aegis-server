package aegis.server.domain.study.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record InstructorStudyApplicationSummary(
        Long studyApplicationId,
        String name,
        String studentId,
        String phoneNumber,
        StudyApplicationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static InstructorStudyApplicationSummary from(StudyApplication studyApplication) {
        return new InstructorStudyApplicationSummary(
                studyApplication.getId(),
                studyApplication.getMember().getName(),
                studyApplication.getMember().getStudentId(),
                studyApplication.getMember().getPhoneNumber(),
                studyApplication.getStatus(),
                studyApplication.getCreatedAt(),
                studyApplication.getUpdatedAt());
    }
}
