package aegis.server.domain.member.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Department;
import aegis.server.domain.member.domain.Grade;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.MemberRecordSource;
import aegis.server.domain.member.domain.Role;

public record AdminMemberRecordItemResponse(
        Long memberRecordId,
        Long memberId,
        String snapshotStudentId,
        String snapshotName,
        String snapshotEmail,
        String snapshotPhoneNumber,
        Department snapshotDepartment,
        Grade snapshotGrade,
        Role snapshotRole,
        YearSemester yearSemester,
        MemberRecordSource recordSource,
        Long paymentId,
        LocalDateTime paymentCompletedAt) {

    public static AdminMemberRecordItemResponse from(MemberRecord memberRecord) {
        return new AdminMemberRecordItemResponse(
                memberRecord.getId(),
                memberRecord.getMember().getId(),
                memberRecord.getSnapshotStudentId(),
                memberRecord.getSnapshotName(),
                memberRecord.getSnapshotEmail(),
                memberRecord.getSnapshotPhoneNumber(),
                memberRecord.getSnapshotDepartment(),
                memberRecord.getSnapshotGrade(),
                memberRecord.getSnapshotRole(),
                memberRecord.getYearSemester(),
                memberRecord.getRecordSource(),
                memberRecord.getPaymentId(),
                memberRecord.getPaymentCompletedAt());
    }
}
