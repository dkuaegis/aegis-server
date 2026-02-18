package aegis.server.domain.member.dto.response;

import aegis.server.domain.common.domain.YearSemester;

public record AdminMemberRecordSemesterOptionResponse(YearSemester yearSemester, String label, boolean current) {

    public static AdminMemberRecordSemesterOptionResponse of(YearSemester yearSemester, boolean current) {
        return new AdminMemberRecordSemesterOptionResponse(yearSemester, yearSemester.getValue(), current);
    }
}
