package aegis.server.domain.study.dto.response;

public record AttendanceCodeIssueResponse(String code, Long sessionId) {

    public static AttendanceCodeIssueResponse from(String code, Long sessionId) {
        return new AttendanceCodeIssueResponse(code, sessionId);
    }
}
