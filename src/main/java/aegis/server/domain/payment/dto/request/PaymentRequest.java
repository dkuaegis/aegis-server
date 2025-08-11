package aegis.server.domain.payment.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(@NotNull List<Long> issuedCouponIds) {}
