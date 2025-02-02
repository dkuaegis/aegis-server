package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CouponCreateRequest(
        @NotBlank String couponName,
        @NotNull @Positive BigDecimal discountAmount
) {
}
