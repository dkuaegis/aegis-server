package aegis.server.domain.coupon.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.coupon.domain.IssuedCoupon;

public record IssuedCouponResponse(
        Long issuedCouponId,
        Long memberId,
        String couponName,
        BigDecimal discountAmount,
        Boolean isValid,
        LocalDateTime createdAt) {
    public static IssuedCouponResponse from(IssuedCoupon issuedCoupon) {
        return new IssuedCouponResponse(
                issuedCoupon.getId(),
                issuedCoupon.getMember().getId(),
                issuedCoupon.getCoupon().getCouponName(),
                issuedCoupon.getCoupon().getDiscountAmount(),
                issuedCoupon.getIsValid(),
                issuedCoupon.getCreatedAt());
    }
}
