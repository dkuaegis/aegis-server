package aegis.server.domain.timetable.repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.timetable.domain.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByYearSemester(YearSemester yearSemester);
}
