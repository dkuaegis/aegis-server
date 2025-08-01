package aegis.server.domain.study.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.study.domain.Study;
import aegis.server.domain.study.domain.StudyMember;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    Optional<StudyMember> findByStudyAndMember(Study study, Member member);
}
