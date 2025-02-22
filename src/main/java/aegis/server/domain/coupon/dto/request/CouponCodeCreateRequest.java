package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponCodeCreateRequest(
        @NotNull Long couponId
) {
}
