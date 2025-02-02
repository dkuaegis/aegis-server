package aegis.server.domain.coupon.dto.response;

import aegis.server.domain.coupon.domain.Coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponResponse(
        Long couponId,
        String couponName,
        BigDecimal discountAmount,
        LocalDateTime createdAt
) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCouponName(),
                coupon.getDiscountAmount(),
                coupon.getCreatedAt()
        );
    }
}
