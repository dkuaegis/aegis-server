package aegis.server.domain.payment.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.PaymentStatus;

public record DevPaymentCreateRequest(
        @NotNull List<Long> issuedCouponIds, @NotNull PaymentStatus status, @NotNull YearSemester yearSemester) {}
