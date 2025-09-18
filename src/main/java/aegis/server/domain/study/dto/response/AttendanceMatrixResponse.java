package aegis.server.domain.study.dto.response;

import java.util.List;

public record AttendanceMatrixResponse(List<AttendanceSessionHeader> sessions, List<AttendanceMemberRow> members) {

    public static AttendanceMatrixResponse from(
            List<AttendanceSessionHeader> sessions, List<AttendanceMemberRow> members) {
        return new AttendanceMatrixResponse(sessions, members);
    }
}
