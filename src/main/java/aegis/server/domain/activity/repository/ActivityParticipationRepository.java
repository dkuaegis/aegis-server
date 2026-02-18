package aegis.server.domain.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.activity.domain.ActivityParticipation;
import aegis.server.domain.common.domain.YearSemester;

public interface ActivityParticipationRepository extends JpaRepository<ActivityParticipation, Long> {

    @EntityGraph(attributePaths = "activity")
    List<ActivityParticipation> findByMemberIdAndActivityYearSemesterOrderByCreatedAtDescIdDesc(
            Long memberId, YearSemester yearSemester);
}
