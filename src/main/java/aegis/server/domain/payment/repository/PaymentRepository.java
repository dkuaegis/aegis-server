package aegis.server.domain.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.member WHERE p.member = :member AND p.yearSemester = :yearSemester")
    Optional<Payment> findByMemberAndYearSemester(
            @Param("member") Member member, @Param("yearSemester") YearSemester yearSemester);

    default Optional<Payment> findByMemberInCurrentYearSemester(Member member) {
        return findByMemberAndYearSemester(member, CURRENT_YEAR_SEMESTER);
    }

    @Query(
            "SELECT p FROM Payment p JOIN FETCH p.member WHERE p.member.name = :memberName AND p.yearSemester = :yearSemester")
    Optional<Payment> findByMemberNameAndYearSemester(
            @Param("memberName") String memberName, @Param("yearSemester") YearSemester yearSemester);

    default Optional<Payment> findByMemberNameInCurrentYearSemester(String memberName) {
        return findByMemberNameAndYearSemester(memberName, CURRENT_YEAR_SEMESTER);
    }

    List<Payment> findAllByStatusAndYearSemester(PaymentStatus paymentStatus, YearSemester currentYearSemester);
}
