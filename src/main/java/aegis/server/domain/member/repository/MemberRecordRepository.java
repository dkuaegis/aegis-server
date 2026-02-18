package aegis.server.domain.member.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.MemberRecord;

public interface MemberRecordRepository extends JpaRepository<MemberRecord, Long> {

    boolean existsByMemberIdAndYearSemester(Long memberId, YearSemester yearSemester);

    @EntityGraph(attributePaths = "member")
    Page<MemberRecord> findByYearSemesterOrderByIdAsc(YearSemester yearSemester, Pageable pageable);

    @EntityGraph(attributePaths = "member")
    List<MemberRecord> findByMemberIdOrderByYearSemesterDescIdDesc(Long memberId);
}
