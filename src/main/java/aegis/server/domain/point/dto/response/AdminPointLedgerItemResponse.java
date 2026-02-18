package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.point.domain.PointTransaction;
import aegis.server.domain.point.domain.PointTransactionType;

public record AdminPointLedgerItemResponse(
        Long pointTransactionId,
        Long memberId,
        String studentId,
        String memberName,
        PointTransactionType transactionType,
        BigDecimal amount,
        String reason,
        LocalDateTime createdAt,
        String idempotencyKey) {

    public static AdminPointLedgerItemResponse from(PointTransaction transaction) {
        Member member = transaction.getPointAccount().getMember();
        return new AdminPointLedgerItemResponse(
                transaction.getId(),
                member.getId(),
                member.getStudentId(),
                member.getName(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getReason(),
                transaction.getCreatedAt(),
                transaction.getIdempotencyKey());
    }
}
