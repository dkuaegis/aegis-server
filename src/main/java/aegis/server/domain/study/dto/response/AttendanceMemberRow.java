package aegis.server.domain.study.dto.response;

import java.util.List;

public record AttendanceMemberRow(Long memberId, String name, List<Boolean> attendance) {

    public static AttendanceMemberRow from(Long memberId, String name, List<Boolean> attendance) {
        return new AttendanceMemberRow(memberId, name, attendance);
    }
}
