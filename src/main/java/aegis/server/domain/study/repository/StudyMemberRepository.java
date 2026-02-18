package aegis.server.domain.study.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyAndMember(Study study, Member member);

    List<StudyMember> findByStudy(Study study);

    @Query(
            "SELECT COUNT(sm) > 0 FROM StudyMember sm WHERE sm.study.id = :studyId AND sm.member.id = :memberId AND sm.role = :role")
    boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole role);

    boolean existsByStudyAndMember(Study study, Member member);

    @Query("""
        SELECT sm
        FROM StudyMember sm
        JOIN FETCH sm.member m
        WHERE sm.study.id = :studyId
          AND sm.role = :role
        ORDER BY m.name ASC
        """)
    List<StudyMember> findByStudyIdAndRoleWithMember(Long studyId, StudyRole role);

    @Query("SELECT sm FROM StudyMember sm WHERE sm.study.id = :studyId AND sm.role = :role")
    Optional<StudyMember> findFirstByStudyIdAndRole(Long studyId, StudyRole role);

    @Query("""
        SELECT s.id
        FROM StudyMember sm
        JOIN sm.study s
        WHERE sm.member.id = :memberId
          AND sm.role = :role
          AND s.yearSemester = :yearSemester
        """)
    List<Long> findStudyIdsByMemberIdAndRoleAndYearSemester(Long memberId, StudyRole role, YearSemester yearSemester);

    @EntityGraph(attributePaths = "study")
    List<StudyMember> findByMemberIdAndStudyYearSemesterOrderByCreatedAtDescIdDesc(
            Long memberId, YearSemester yearSemester);
}
