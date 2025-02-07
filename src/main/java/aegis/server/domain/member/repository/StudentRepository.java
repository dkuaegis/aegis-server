package aegis.server.domain.member.repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByMember(Member member);

    Optional<Student> findByMemberAndYearSemester(Member member, YearSemester yearSemester);

    default Optional<Student> findByMemberInCurrentYearSemester(Member member) {
        return findByMemberAndYearSemester(member, CURRENT_YEAR_SEMESTER);
    }

    Optional<Student> findByMember_IdAndYearSemester(Long memberId, YearSemester yearSemester);

    default Optional<Student> findByMemberIdInCurrentYearSemester(Long memberId) {
        return findByMember_IdAndYearSemester(memberId, CURRENT_YEAR_SEMESTER);
    }
}
