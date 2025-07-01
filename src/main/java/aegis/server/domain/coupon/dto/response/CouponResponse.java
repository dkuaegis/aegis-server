package aegis.server.domain.coupon.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.coupon.domain.Coupon;

public record CouponResponse(Long couponId, String couponName, BigDecimal discountAmount, LocalDateTime createdAt) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(), coupon.getCouponName(), coupon.getDiscountAmount(), coupon.getCreatedAt());
    }
}
