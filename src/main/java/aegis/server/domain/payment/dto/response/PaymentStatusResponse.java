package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record PaymentStatusResponse(PaymentCheckStatus status, BigDecimal finalPrice) {

    public static PaymentStatusResponse from(Payment payment) {
        return new PaymentStatusResponse(toPaymentCheckStatus(payment.getStatus()), payment.getFinalPrice());
    }

    public static PaymentStatusResponse notCreated() {
        return new PaymentStatusResponse(PaymentCheckStatus.NOT_CREATED, BigDecimal.ZERO);
    }

    public static PaymentStatusResponse completed() {
        return new PaymentStatusResponse(PaymentCheckStatus.COMPLETED, BigDecimal.ZERO);
    }

    private static PaymentCheckStatus toPaymentCheckStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case PENDING -> PaymentCheckStatus.PENDING;
            case COMPLETED -> PaymentCheckStatus.COMPLETED;
        };
    }
}
