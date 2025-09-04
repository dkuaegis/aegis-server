package aegis.server.domain.pointshop.dto.response;

import java.math.BigDecimal;

import aegis.server.domain.pointshop.domain.PointShopItem;

public record PointShopDrawResponse(
        PointShopItem item, BigDecimal remainingBalance, Long transactionId, Long drawHistoryId) {

    public static PointShopDrawResponse of(
            PointShopItem item, BigDecimal remainingBalance, Long transactionId, Long drawHistoryId) {
        return new PointShopDrawResponse(item, remainingBalance, transactionId, drawHistoryId);
    }
}
