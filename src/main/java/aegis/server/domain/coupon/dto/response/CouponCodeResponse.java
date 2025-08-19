package aegis.server.domain.coupon.dto.response;

import aegis.server.domain.coupon.domain.CouponCode;

public record CouponCodeResponse(Long codeCouponId, Long couponId, String couponName, String code, Boolean isValid) {

    public static CouponCodeResponse from(CouponCode couponCode) {
        return new CouponCodeResponse(
                couponCode.getId(),
                couponCode.getCoupon().getId(),
                couponCode.getCoupon().getCouponName(),
                couponCode.getCode(),
                couponCode.getIsValid());
    }
}
