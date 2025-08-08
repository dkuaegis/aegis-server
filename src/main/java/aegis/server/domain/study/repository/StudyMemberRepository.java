package aegis.server.domain.study.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyMember;
import aegis.server.domain.study.domain.StudyRole;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyAndMember(Study study, Member member);

    @Query(
            "SELECT COUNT(sm) > 0 FROM StudyMember sm WHERE sm.study.id = :studyId AND sm.member.id = :memberId AND sm.role = :role")
    boolean existsByStudyIdAndMemberIdAndRole(Long studyId, Long memberId, StudyRole role);

    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.member WHERE sm.study.id = :studyId AND sm.role = 'INSTRUCTOR'")
    Optional<StudyMember> findInstructorByStudyId(Long studyId);

    @Query("SELECT COUNT(sm) FROM StudyMember sm WHERE sm.study.id = :studyId AND sm.role = 'PARTICIPANT'")
    long countParticipantsByStudyId(Long studyId);
}
