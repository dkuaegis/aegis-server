package aegis.server.domain.coupon.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.coupon.domain.IssuedCoupon;

public interface IssuedCouponQueryRepository {

    Page<IssuedCoupon> searchAdminIssuedCoupons(
            String keyword, Long couponId, Long memberId, Boolean isValid, Pageable pageable, String orderByClause);
}
