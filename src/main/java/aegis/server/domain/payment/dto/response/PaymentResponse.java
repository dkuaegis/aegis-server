package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record PaymentResponse(Long id, PaymentStatus status, BigDecimal finalPrice) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getStatus(), payment.getFinalPrice());
    }
}
