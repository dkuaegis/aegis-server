package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record DevPaymentResponse(
        Long id,
        String memberName,
        PaymentStatus status,
        YearSemester yearSemester,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static DevPaymentResponse from(Payment payment) {
        return new DevPaymentResponse(
                payment.getId(),
                payment.getMember().getName(),
                payment.getStatus(),
                payment.getYearSemester(),
                payment.getOriginalPrice(),
                payment.getFinalPrice(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
