package aegis.server.domain.study.dto.response;

import java.time.LocalDate;

public record AttendanceSessionHeader(Long sessionId, LocalDate date) {

    public static AttendanceSessionHeader from(Long sessionId, LocalDate date) {
        return new AttendanceSessionHeader(sessionId, date);
    }
}
