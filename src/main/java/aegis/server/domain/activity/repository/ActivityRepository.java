package aegis.server.domain.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.common.domain.YearSemester;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    boolean existsByNameAndYearSemester(String name, YearSemester yearSemester);
}
