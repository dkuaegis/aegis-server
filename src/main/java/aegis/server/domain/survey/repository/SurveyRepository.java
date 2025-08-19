package aegis.server.domain.survey.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.survey.domain.Survey;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query("select s from Survey s where s.member.id = :memberId and s.yearSemester = :yearSemester")
    Optional<Survey> findByMemberIdAndYearSemester(Long memberId, YearSemester yearSemester);

    default Optional<Survey> findByMemberIdInCurrentYearSemester(Long memberId) {
        return findByMemberIdAndYearSemester(memberId, CURRENT_YEAR_SEMESTER);
    }
}
