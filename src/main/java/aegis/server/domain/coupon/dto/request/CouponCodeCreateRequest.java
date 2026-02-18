package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CouponCodeCreateRequest(
        @NotNull Long couponId, @Size(max = 255) String description) {}
