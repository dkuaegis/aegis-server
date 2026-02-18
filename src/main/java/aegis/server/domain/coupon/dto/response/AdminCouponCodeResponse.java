package aegis.server.domain.coupon.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.coupon.domain.CouponCode;

public record AdminCouponCodeResponse(
        Long codeCouponId,
        Long couponId,
        String couponName,
        String code,
        String description,
        Boolean isValid,
        Long issuedCouponId,
        LocalDateTime usedAt,
        LocalDateTime createdAt) {

    public static AdminCouponCodeResponse from(CouponCode couponCode) {
        Long issuedCouponId = couponCode.getIssuedCoupon() == null
                ? null
                : couponCode.getIssuedCoupon().getId();
        return new AdminCouponCodeResponse(
                couponCode.getId(),
                couponCode.getCoupon().getId(),
                couponCode.getCoupon().getCouponName(),
                couponCode.getCode(),
                couponCode.getDescription(),
                couponCode.getIsValid(),
                issuedCouponId,
                couponCode.getUsedAt(),
                couponCode.getCreatedAt());
    }
}
