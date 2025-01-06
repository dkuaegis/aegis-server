package aegis.server.domain.coupon.repository;

import aegis.server.domain.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsCouponByCouponName(String couponName);
}
