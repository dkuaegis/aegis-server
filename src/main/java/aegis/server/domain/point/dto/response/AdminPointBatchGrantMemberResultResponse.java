package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;

public record AdminPointBatchGrantMemberResultResponse(
        Long memberId,
        AdminPointBatchGrantStatus status,
        Long pointTransactionId,
        BigDecimal newBalance,
        String errorName) {

    public static AdminPointBatchGrantMemberResultResponse success(
            Long memberId, Long pointTransactionId, BigDecimal newBalance) {
        return new AdminPointBatchGrantMemberResultResponse(
                memberId, AdminPointBatchGrantStatus.SUCCESS, pointTransactionId, newBalance, null);
    }

    public static AdminPointBatchGrantMemberResultResponse duplicate(Long memberId, BigDecimal newBalance) {
        return new AdminPointBatchGrantMemberResultResponse(
                memberId, AdminPointBatchGrantStatus.DUPLICATE, null, newBalance, null);
    }

    public static AdminPointBatchGrantMemberResultResponse failure(Long memberId, String errorName) {
        return new AdminPointBatchGrantMemberResultResponse(
                memberId, AdminPointBatchGrantStatus.FAILED, null, null, errorName);
    }
}
