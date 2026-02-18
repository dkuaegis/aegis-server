package aegis.server.domain.coupon.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import aegis.server.domain.coupon.domain.CouponCode;

public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {

    @Query("SELECT cc FROM CouponCode cc WHERE cc.code = :code")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CouponCode> findByCodeWithLock(String code);

    @Query("SELECT cc FROM CouponCode cc JOIN FETCH cc.coupon LEFT JOIN FETCH cc.issuedCoupon")
    List<CouponCode> findAllWithCouponAndIssuedCoupon();

    @Query("SELECT cc FROM CouponCode cc JOIN FETCH cc.coupon LEFT JOIN FETCH cc.issuedCoupon WHERE cc.id = :id")
    Optional<CouponCode> findByIdWithCouponAndIssuedCoupon(Long id);

    boolean existsByCode(String code);
}
