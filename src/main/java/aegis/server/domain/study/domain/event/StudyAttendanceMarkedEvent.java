package aegis.server.domain.study.domain.event;

public record StudyAttendanceMarkedEvent(Long studyId, Long sessionId, Long participantId) {}
