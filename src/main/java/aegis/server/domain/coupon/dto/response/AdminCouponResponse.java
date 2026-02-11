package aegis.server.domain.coupon.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.coupon.domain.Coupon;

public record AdminCouponResponse(
        Long couponId, String couponName, BigDecimal discountAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static AdminCouponResponse from(Coupon coupon) {
        return new AdminCouponResponse(
                coupon.getId(),
                coupon.getCouponName(),
                coupon.getDiscountAmount(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt());
    }
}
