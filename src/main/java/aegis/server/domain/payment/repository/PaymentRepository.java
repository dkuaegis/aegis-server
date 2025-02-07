package aegis.server.domain.payment.repository;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Student;
import aegis.server.domain.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStudentAndYearSemester(Student student, YearSemester yearSemester);

    default Optional<Payment> findByStudentInCurrentYearSemester(Student student) {
        return findByStudentAndYearSemester(student, CURRENT_YEAR_SEMESTER);
    }

    Optional<Payment> findByExpectedDepositorNameAndYearSemester(String expectedDepositorName, YearSemester yearSemester);

    default Optional<Payment> findByExpectedDepositorNameInCurrentYearSemester(String expectedDepositorName) {
        return findByExpectedDepositorNameAndYearSemester(expectedDepositorName, CURRENT_YEAR_SEMESTER);
    }
}
