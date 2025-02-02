package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CouponIssueRequest(
        @NotNull Long couponId,
        @NotEmpty List<Long> memberIds
) {
}
