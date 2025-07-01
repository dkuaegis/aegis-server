package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class PaymentStatusResponse {

    private final PaymentStatus status;

    private final BigDecimal expectedDepositAmount;

    private final BigDecimal currentDepositAmount;

    public static PaymentStatusResponse from(Payment payment, BigDecimal currentDepositAmount) {
        return PaymentStatusResponse.builder()
                .status(payment.getStatus())
                .expectedDepositAmount(payment.getFinalPrice())
                .currentDepositAmount(currentDepositAmount)
                .build();
    }
}
