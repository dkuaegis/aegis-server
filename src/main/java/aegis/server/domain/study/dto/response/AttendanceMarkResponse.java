package aegis.server.domain.study.dto.response;

public record AttendanceMarkResponse(Long attendanceId, Long sessionId) {

    public static AttendanceMarkResponse from(Long attendanceId, Long sessionId) {
        return new AttendanceMarkResponse(attendanceId, sessionId);
    }
}
