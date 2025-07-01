package aegis.server.domain.coupon.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CouponIssueRequest(@NotNull Long couponId, @NotEmpty List<Long> memberIds) {}
