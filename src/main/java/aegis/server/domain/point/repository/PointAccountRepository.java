package aegis.server.domain.point.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.point.domain.PointAccount;

public interface PointAccountRepository extends JpaRepository<PointAccount, Long> {
    boolean existsByMember(Member member);

    @Query("SELECT pa FROM PointAccount pa JOIN FETCH pa.member WHERE pa.member.id = :memberId")
    Optional<PointAccount> findByMemberId(Long memberId);

    @Query("SELECT pa FROM PointAccount pa JOIN FETCH pa.member "
            + "WHERE EXISTS (SELECT 1 FROM Payment p WHERE p.member = pa.member AND p.yearSemester = :yearSemester AND p.status = :status) "
            + "ORDER BY pa.totalEarned DESC")
    List<PointAccount> findTopByEligible(YearSemester yearSemester, PaymentStatus status, Pageable pageable);

    @Query(
            "SELECT COUNT(pa) FROM PointAccount pa "
                    + "WHERE pa.totalEarned > :totalEarnedThreshold "
                    + "AND EXISTS (SELECT 1 FROM Payment p WHERE p.member = pa.member AND p.yearSemester = :yearSemester AND p.status = :status)")
    long countEligibleWithTotalEarnedGreaterThan(
            YearSemester yearSemester, PaymentStatus status, BigDecimal totalEarnedThreshold);
}
