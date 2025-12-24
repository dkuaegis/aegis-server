package aegis.server.domain.payment.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

import static aegis.server.global.constant.Constant.CURRENT_YEAR_SEMESTER;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.member WHERE p.member = :member AND p.yearSemester = :yearSemester")
    Optional<Payment> findByMemberAndYearSemester(Member member, YearSemester yearSemester);

    default Optional<Payment> findByMemberInCurrentYearSemester(Member member) {
        return findByMemberAndYearSemester(member, CURRENT_YEAR_SEMESTER);
    }

    @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId AND p.yearSemester = :yearSemester")
    Optional<Payment> findByMemberIdAndYearSemester(Long memberId, YearSemester yearSemester);

    default Optional<Payment> findByMemberIdInCurrentYearSemester(Long memberId) {
        return findByMemberIdAndYearSemester(memberId, CURRENT_YEAR_SEMESTER);
    }

    @Query(
            "SELECT p FROM Payment p WHERE p.member.id = :memberId AND p.yearSemester = :yearSemester AND p.status = :status")
    Optional<Payment> findByMemberIdAndYearSemesterAndStatus(
            Long memberId, YearSemester yearSemester, PaymentStatus status);

    default Optional<Payment> findByMemberIdAndCurrentYearSemesterAndStatusIsPending(Long memberId) {
        return findByMemberIdAndYearSemesterAndStatus(memberId, CURRENT_YEAR_SEMESTER, PaymentStatus.PENDING);
    }

    boolean existsByMemberIdAndYearSemester(Long memberId, YearSemester yearSemester);

    default boolean existsByMemberIdAndCurrentYearSemester(Long memberId) {
        return existsByMemberIdAndYearSemester(memberId, CURRENT_YEAR_SEMESTER);
    }

    @Query(
            "SELECT p FROM Payment p WHERE p.member.name = :memberName AND p.finalPrice = :finalPrice AND p.yearSemester = :yearSemester AND p.status = :status")
    Optional<Payment> findByMemberNameAndFinalPriceAndYearSemesterAndStatus(
            String memberName, BigDecimal finalPrice, YearSemester yearSemester, PaymentStatus status);

    default Optional<Payment> findPendingPaymentForCurrentSemester(String memberName, BigDecimal finalPrice) {
        return findByMemberNameAndFinalPriceAndYearSemesterAndStatus(
                memberName, finalPrice, CURRENT_YEAR_SEMESTER, PaymentStatus.PENDING);
    }

    @Query(
            "SELECT p FROM Payment p WHERE p.member.name = :memberName AND p.finalPrice = :finalPrice AND p.yearSemester = :yearSemester AND p.status = :status")
    List<Payment> findAllByMemberNameAndFinalPriceAndYearSemesterAndStatus(
            String memberName, BigDecimal finalPrice, YearSemester yearSemester, PaymentStatus status);

    default List<Payment> findAllPendingPaymentsForCurrentSemester(String memberName, BigDecimal finalPrice) {
        return findAllByMemberNameAndFinalPriceAndYearSemesterAndStatus(
                memberName, finalPrice, CURRENT_YEAR_SEMESTER, PaymentStatus.PENDING);
    }

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.yearSemester = :yearSemester AND p.status = :status")
    long countByYearSemesterAndStatus(YearSemester yearSemester, PaymentStatus status);

    default long countCompletedPaymentsInCurrentYearSemester() {
        return countByYearSemesterAndStatus(CURRENT_YEAR_SEMESTER, PaymentStatus.COMPLETED);
    }

    List<Payment> findAllByStatusAndYearSemester(PaymentStatus paymentStatus, YearSemester currentYearSemester);

    @Query(
            "SELECT p FROM Payment p JOIN FETCH p.member WHERE p.member.id = :memberId ORDER BY p.yearSemester DESC, p.createdAt DESC")
    List<Payment> findAllByMemberIdOrderByYearSemesterDescCreatedAtDesc(Long memberId);
}
