package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CouponNameUpdateRequest(@NotBlank String couponName) {}
