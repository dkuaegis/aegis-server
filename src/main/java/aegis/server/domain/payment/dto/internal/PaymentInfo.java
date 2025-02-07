package aegis.server.domain.payment.dto.internal;

import aegis.server.domain.payment.domain.Payment;

public record PaymentInfo(
        Long id,
        Long studentId,
        Long memberId
) {
    public static PaymentInfo from(Payment payment) {
        return new PaymentInfo(
                payment.getId(),
                payment.getStudent().getId(),
                payment.getStudent().getMember().getId()
        );
    }
}
