package aegis.server.global.security.controller;

import aegis.server.domain.payment.domain.PaymentStatus;

public record AuthCheckResponse(
        PaymentStatus paymentStatus
) {
}
