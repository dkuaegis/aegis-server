package aegis.server.domain.member.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.MemberRecord;

public interface MemberRecordRepository extends JpaRepository<MemberRecord, Long>, MemberRecordQueryRepository {

    boolean existsByMemberIdAndYearSemester(Long memberId, YearSemester yearSemester);

    @EntityGraph(attributePaths = "member")
    List<MemberRecord> findByMemberIdOrderByYearSemesterDescIdDesc(Long memberId);
}
