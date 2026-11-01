package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.util.List;

import aegis.server.domain.coupon.domain.IssuedCoupon;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

public record PaymentStatusResponse(PaymentCheckStatus status, BigDecimal finalPrice, List<Long> issuedCouponIds) {

    public static PaymentStatusResponse from(Payment payment) {
        return new PaymentStatusResponse(
                toPaymentCheckStatus(payment.getStatus()),
                payment.getFinalPrice(),
                payment.getUsedCoupons().stream()
                        .map(IssuedCoupon::getId)
                        .sorted()
                        .toList());
    }

    public static PaymentStatusResponse notCreated() {
        return new PaymentStatusResponse(PaymentCheckStatus.NOT_CREATED, BigDecimal.ZERO, List.of());
    }

    public static PaymentStatusResponse completed() {
        return new PaymentStatusResponse(PaymentCheckStatus.COMPLETED, BigDecimal.ZERO, List.of());
    }

    private static PaymentCheckStatus toPaymentCheckStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case PENDING -> PaymentCheckStatus.PENDING;
            case COMPLETED -> PaymentCheckStatus.COMPLETED;
        };
    }
}
