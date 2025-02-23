package aegis.server.domain.timetable.repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.timetable.domain.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    Optional<Timetable> findByMemberAndYearSemester(Member member, YearSemester yearSemester);

    Optional<Timetable> findByIdentifierAndYearSemester(String identifier, YearSemester yearSemester);
}