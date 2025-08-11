package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record PaymentStatusResponse(PaymentStatus status, BigDecimal finalPrice) {

    public static PaymentStatusResponse from(Payment payment) {
        return new PaymentStatusResponse(payment.getStatus(), payment.getFinalPrice());
    }
}
