package aegis.server.domain.coupon.dto.response;

import aegis.server.domain.coupon.domain.Coupon;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponResponse {

    private Long couponId;

    private String couponName;

    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getId(), coupon.getCouponName());
    }
}
