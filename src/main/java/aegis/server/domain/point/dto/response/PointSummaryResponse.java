package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;
import java.util.List;

import aegis.server.domain.point.domain.PointAccount;
import aegis.server.domain.point.domain.PointTransaction;

public record PointSummaryResponse(BigDecimal balance, List<PointTransactionResponse> history) {
    public static PointSummaryResponse from(PointAccount account, List<PointTransaction> transactions) {
        return new PointSummaryResponse(
                account.getBalance(),
                transactions.stream().map(PointTransactionResponse::from).toList());
    }
}
