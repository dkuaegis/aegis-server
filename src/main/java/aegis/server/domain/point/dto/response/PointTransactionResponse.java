package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;

public record PointTransactionResponse(
        Long pointTransactionId,
        PointTransactionType transactionType,
        BigDecimal amount,
        String reason,
        LocalDateTime createdAt) {
    public static PointTransactionResponse from(PointTransaction pointTransaction) {
        return new PointTransactionResponse(
                pointTransaction.getId(),
                pointTransaction.getTransactionType(),
                pointTransaction.getAmount(),
                pointTransaction.getReason(),
                pointTransaction.getCreatedAt());
    }
}
