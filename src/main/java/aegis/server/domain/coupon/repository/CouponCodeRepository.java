package aegis.server.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.coupon.domain.CouponCode;

public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {
    Optional<CouponCode> findByCode(String code);

    boolean existsByCode(String code);
}
