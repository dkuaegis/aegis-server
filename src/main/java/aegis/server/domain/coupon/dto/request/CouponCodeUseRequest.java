package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record CouponCodeUseRequest(@NotEmpty String code) {}
