package aegis.server.domain.survey.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.survey.domain.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByMember(Member member);

    List<Survey> findByMemberIn(List<Member> members);
}
