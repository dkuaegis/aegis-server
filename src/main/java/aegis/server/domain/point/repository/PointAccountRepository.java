package aegis.server.domain.point.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;
import aegis.server.domain.payment.domain.PaymentStatus;
import aegis.server.domain.point.domain.PointAccount;

public interface PointAccountRepository extends JpaRepository<PointAccount, Long> {
    boolean existsByMember(Member member);

    @Query("SELECT pa FROM PointAccount pa JOIN FETCH pa.member WHERE pa.member.id = :memberId")
    Optional<PointAccount> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pa FROM PointAccount pa WHERE pa.id = :id")
    Optional<PointAccount> findByIdWithLock(Long id);

    @Query("SELECT pa FROM PointAccount pa JOIN FETCH pa.member "
            + "WHERE pa.member.role <> :excludedRole "
            + "AND EXISTS (SELECT 1 FROM Payment p WHERE p.member = pa.member AND p.yearSemester = :yearSemester AND p.status = :status) "
            + "ORDER BY pa.totalEarned DESC")
    List<PointAccount> findTopByEligibleExcludingRole(
            YearSemester yearSemester, PaymentStatus status, Role excludedRole, Pageable pageable);

    @Query(
            "SELECT COUNT(pa) FROM PointAccount pa "
                    + "WHERE pa.totalEarned > :totalEarnedThreshold "
                    + "AND pa.member.role <> :excludedRole "
                    + "AND EXISTS (SELECT 1 FROM Payment p WHERE p.member = pa.member AND p.yearSemester = :yearSemester AND p.status = :status)")
    long countEligibleWithTotalEarnedGreaterThanExcludingRole(
            YearSemester yearSemester, PaymentStatus status, Role excludedRole, BigDecimal totalEarnedThreshold);
}
