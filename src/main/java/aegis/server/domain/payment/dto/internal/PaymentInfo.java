package aegis.server.domain.payment.dto.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;

public record PaymentInfo(
        Long id,
        Long memberId,
        YearSemester yearSemester,
        BigDecimal finalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getMember().getId(),
                payment.getYearSemester(),
                payment.getFinalPrice(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
