package aegis.server.domain.payment.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.PaymentStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DevPaymentUpdateRequest {

    private List<Long> issuedCouponIds;
    private PaymentStatus status;
    private YearSemester yearSemester;
}
