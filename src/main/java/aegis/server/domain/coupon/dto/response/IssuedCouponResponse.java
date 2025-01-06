package aegis.server.domain.coupon.dto.response;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class IssuedCouponResponse {

    private Long issuedCouponId;

    private Long memberId;
    
    private String couponName;

    private BigDecimal discountAmount;


    public static IssuedCouponResponse from(IssuedCoupon issuedCoupon) {
        return new IssuedCouponResponse(
                issuedCoupon.getId(),
                issuedCoupon.getMember().getId(),
                issuedCoupon.getCoupon().getCouponName(),
                issuedCoupon.getCoupon().getDiscountAmount()
        );
    }
}
