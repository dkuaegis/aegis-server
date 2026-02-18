package aegis.server.domain.member.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.MemberRecord;
import aegis.server.domain.member.domain.Role;

public interface MemberRecordQueryRepository {

    Page<MemberRecord> searchByYearSemesterForAdmin(
            YearSemester yearSemester, String keyword, Role role, Pageable pageable);
}
