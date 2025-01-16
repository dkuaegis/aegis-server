package aegis.server.domain.survey.repository;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByMemberId(Long memberId);
    Optional<Survey> findByIdAndCurrentSemester(Long id, String currentSemester);

}
