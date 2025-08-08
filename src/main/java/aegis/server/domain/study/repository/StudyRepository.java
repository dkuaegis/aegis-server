package aegis.server.domain.study.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.study.domain.Study;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface StudyRepository extends JpaRepository<Study, Long> {

    @Query("SELECT s FROM Study s WHERE s.yearSemester = :yearSemester")
    List<Study> findByYearSemester(YearSemester yearSemester);

    default List<Study> findByCurrentYearSemester() {
        return findByYearSemester(CURRENT_YEAR_SEMESTER);
    }
}
