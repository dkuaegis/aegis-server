package aegis.server.domain.member.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Department;
import aegis.server.domain.member.domain.Grade;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.domain.Role;

public record AdminMemberRecordTimelineResponse(
        Long memberRecordId,
        YearSemester yearSemester,
        MemberRecordSource recordSource,
        String snapshotStudentId,
        String snapshotName,
        String snapshotEmail,
        String snapshotPhoneNumber,
        Department snapshotDepartment,
        Grade snapshotGrade,
        Role snapshotRole,
        Long paymentId,
        LocalDateTime paymentCompletedAt) {

    public static AdminMemberRecordTimelineResponse from(MemberRecord memberRecord) {
        return new AdminMemberRecordTimelineResponse(
                memberRecord.getId(),
                memberRecord.getYearSemester(),
                memberRecord.getRecordSource(),
                memberRecord.getSnapshotStudentId(),
                memberRecord.getSnapshotName(),
                memberRecord.getSnapshotEmail(),
                memberRecord.getSnapshotPhoneNumber(),
                memberRecord.getSnapshotDepartment(),
                memberRecord.getSnapshotGrade(),
                memberRecord.getSnapshotRole(),
                memberRecord.getPaymentId(),
                memberRecord.getPaymentCompletedAt());
    }
}
