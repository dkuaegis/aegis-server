package aegis.server.domain.coupon.repository;

import aegis.server.domain.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCouponNameAndDiscountAmount(String couponName, BigDecimal discountAmount);
}
