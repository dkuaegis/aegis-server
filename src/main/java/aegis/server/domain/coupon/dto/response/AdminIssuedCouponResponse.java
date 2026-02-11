package aegis.server.domain.coupon.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.coupon.domain.IssuedCoupon;

public record AdminIssuedCouponResponse(
        Long issuedCouponId,
        Long couponId,
        String couponName,
        BigDecimal discountAmount,
        Long memberId,
        String memberName,
        String memberEmail,
        Boolean isValid,
        Long paymentId,
        LocalDateTime usedAt,
        LocalDateTime createdAt) {

    public static AdminIssuedCouponResponse from(IssuedCoupon issuedCoupon) {
        Long paymentId = issuedCoupon.getPayment() == null
                ? null
                : issuedCoupon.getPayment().getId();
        return new AdminIssuedCouponResponse(
                issuedCoupon.getId(),
                issuedCoupon.getCoupon().getId(),
                issuedCoupon.getCoupon().getCouponName(),
                issuedCoupon.getCoupon().getDiscountAmount(),
                issuedCoupon.getMember().getId(),
                issuedCoupon.getMember().getName(),
                issuedCoupon.getMember().getEmail(),
                issuedCoupon.getIsValid(),
                paymentId,
                issuedCoupon.getUsedAt(),
                issuedCoupon.getCreatedAt());
    }
}
