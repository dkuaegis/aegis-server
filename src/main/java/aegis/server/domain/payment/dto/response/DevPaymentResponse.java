package aegis.server.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import aegis.server.domain.common.domain.YearSemester;
import aegis.server.domain.payment.domain.Payment;
import aegis.server.domain.payment.domain.PaymentStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DevPaymentResponse {

    private final Long id;
    private final String memberName;
    private final PaymentStatus status;
    private final YearSemester yearSemester;
    private final BigDecimal originalPrice;
    private final BigDecimal finalPrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
