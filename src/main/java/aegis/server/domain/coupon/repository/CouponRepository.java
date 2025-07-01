package aegis.server.domain.coupon.repository;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;

import aegis.server.domain.coupon.domain.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCouponNameAndDiscountAmount(String couponName, BigDecimal discountAmount);
}
