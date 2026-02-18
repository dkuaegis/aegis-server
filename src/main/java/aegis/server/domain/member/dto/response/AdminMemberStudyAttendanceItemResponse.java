package aegis.server.domain.member.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import aegis.server.domain.study.domain.StudyAttendance;

public record AdminMemberStudyAttendanceItemResponse(
        Long studyAttendanceId,
        Long studyId,
        String studyTitle,
        Long studySessionId,
        LocalDate sessionDate,
        LocalDateTime attendedAt) {

    public static AdminMemberStudyAttendanceItemResponse from(StudyAttendance studyAttendance) {
        return new AdminMemberStudyAttendanceItemResponse(
                studyAttendance.getId(),
                studyAttendance.getStudySession().getStudy().getId(),
                studyAttendance.getStudySession().getStudy().getTitle(),
                studyAttendance.getStudySession().getId(),
                studyAttendance.getStudySession().getSessionDate(),
                studyAttendance.getCreatedAt());
    }
}
