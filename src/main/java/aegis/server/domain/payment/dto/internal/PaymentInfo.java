package aegis.server.domain.payment.dto.internal;

import aegis.server.domain.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentInfo(
        Long id,
        Long studentId,
        Long memberId,
        BigDecimal finalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getStudent().getId(),
                payment.getStudent().getMember().getId(),
                payment.getFinalPrice(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
