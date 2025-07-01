package aegis.server.domain.coupon.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CouponCreateRequest(
        @NotBlank String couponName,
        @NotNull @Positive BigDecimal discountAmount
) {
}
