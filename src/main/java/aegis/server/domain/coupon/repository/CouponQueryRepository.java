package aegis.server.domain.coupon.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.coupon.domain.Coupon;

public interface CouponQueryRepository {

    Page<Coupon> searchAdminCoupons(String keyword, Pageable pageable, String orderByClause);
}
