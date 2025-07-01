package aegis.server.domain.payment.domain.event;

import aegis.server.domain.payment.dto.internal.PaymentInfo;

public record PaymentCompletedEvent(PaymentInfo paymentInfo) {}
