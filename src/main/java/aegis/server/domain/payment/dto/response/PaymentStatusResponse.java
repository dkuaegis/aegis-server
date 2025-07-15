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

    private final BigDecimal finalPrice;

    public static PaymentStatusResponse from(Payment payment) {
        return PaymentStatusResponse.builder()
                .status(payment.getStatus())
                .finalPrice(payment.getFinalPrice())
                .build();
    }
}
