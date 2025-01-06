package aegis.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueRequest {

    private Long couponId;

    @NotEmpty
    private List<Long> memberIds;
}
