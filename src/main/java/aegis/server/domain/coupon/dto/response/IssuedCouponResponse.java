package aegis.server.domain.coupon.dto.response;

import aegis.server.domain.coupon.domain.IssuedCoupon;

import java.math.BigDecimal;

public record IssuedCouponResponse(
        Long issuedCouponId,
        Long memberId,
        String couponName,
        BigDecimal discountAmount
) {
    public static IssuedCouponResponse from(IssuedCoupon issuedCoupon) {
        return new IssuedCouponResponse(
                issuedCoupon.getId(),
                issuedCoupon.getMember().getId(),
                issuedCoupon.getCoupon().getCouponName(),
                issuedCoupon.getCoupon().getDiscountAmount()
        );
    }
}
