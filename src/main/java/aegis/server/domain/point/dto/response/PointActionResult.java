package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;

public record PointActionResult(Long transactionId, BigDecimal accountBalance, boolean applied) {
    public static PointActionResult of(Long transactionId, BigDecimal accountBalance, boolean applied) {
        return new PointActionResult(transactionId, accountBalance, applied);
    }
}
