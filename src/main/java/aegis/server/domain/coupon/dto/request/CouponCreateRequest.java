package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank
    private String couponName;

    @Positive
    private Long discountAmount;
}
