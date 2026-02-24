package aegis.server.domain.coupon.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import aegis.server.domain.coupon.domain.CouponCode;

public interface CouponCodeQueryRepository {

    Page<CouponCode> searchAdminCouponCodes(String keyword, Pageable pageable, String orderByClause);
}
