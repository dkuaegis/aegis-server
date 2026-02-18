package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;

public record AdminPointGrantResultResponse(
        boolean created, Long pointTransactionId, Long memberId, BigDecimal newBalance) {

    public static AdminPointGrantResultResponse of(PointActionResult result, Long memberId) {
        return new AdminPointGrantResultResponse(
                result.applied(), result.transactionId(), memberId, result.accountBalance());
    }
}
