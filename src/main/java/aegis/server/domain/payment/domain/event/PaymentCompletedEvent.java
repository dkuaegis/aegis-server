package aegis.server.domain.payment.domain.event;

import aegis.server.domain.payment.domain.Payment;

public record PaymentCompletedEvent(Payment payment) {
}
