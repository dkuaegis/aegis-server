package aegis.server.domain.pointshop.dto.response;

import java.time.LocalDateTime;

import aegis.server.domain.pointshop.domain.PointShopDrawHistory;
import aegis.server.domain.pointshop.domain.PointShopItem;

public record PointShopDrawHistoryResponse(
        Long drawHistoryId, PointShopItem item, Long transactionId, LocalDateTime createdAt) {

    public static PointShopDrawHistoryResponse from(PointShopDrawHistory history) {
        return new PointShopDrawHistoryResponse(
                history.getId(),
                history.getItem(),
                history.getPointTransaction().getId(),
                history.getCreatedAt());
    }
}
