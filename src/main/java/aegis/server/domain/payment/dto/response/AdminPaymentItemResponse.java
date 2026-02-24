package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record AdminPaymentItemResponse(
        Long paymentId,
        Long memberId,
        String memberName,
        String studentId,
        YearSemester yearSemester,
        PaymentStatus status,
        BigDecimal originalPrice,
        BigDecimal finalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static AdminPaymentItemResponse from(Payment payment) {
        return new AdminPaymentItemResponse(
                payment.getId(),
                payment.getMember().getId(),
                payment.getMember().getName(),
                payment.getMember().getStudentId(),
                payment.getYearSemester(),
                payment.getStatus(),
                payment.getOriginalPrice(),
                payment.getFinalPrice(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
