package aegis.server.domain.study.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.study.domain.StudyApplication;
import aegis.server.domain.study.domain.StudyApplicationStatus;

public record InstructorStudyApplicationSummary(
        Long studyApplicationId,
        String name,
        @Schema(nullable = true) String studentId,
        @Schema(nullable = true) String phoneNumber,
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
