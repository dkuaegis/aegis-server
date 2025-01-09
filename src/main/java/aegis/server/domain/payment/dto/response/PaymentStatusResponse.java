package aegis.server.domain.payment.dto.response;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PaymentStatusResponse {

    private final PaymentStatus status;

    private final BigDecimal expectedDepositAmount;

    private final BigDecimal currentDepositAmount;

    public static PaymentStatusResponse from(Payment payment) {
        return PaymentStatusResponse.builder()
                .status(payment.getStatus())
                .expectedDepositAmount(payment.getFinalPrice())
                .currentDepositAmount(payment.getCurrentDepositAmount())
                .build();
    }
}
