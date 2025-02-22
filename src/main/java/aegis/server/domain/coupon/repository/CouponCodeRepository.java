package aegis.server.domain.coupon.repository;

import aegis.server.domain.coupon.domain.CouponCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponCodeRepository extends JpaRepository<CouponCode, Long> {
    Optional<CouponCode> findByCode(String code);

    boolean existsByCode(String code);
}
