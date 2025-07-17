package aegis.server.domain.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.activity.domain.Activity;
import aegis.server.domain.common.domain.YearSemester;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByIsActive(Boolean isActive);

    default List<Activity> findActiveActivities() {
        return findByIsActive(true);
    }

    boolean existsByNameAndYearSemester(String name, YearSemester yearSemester);
}
