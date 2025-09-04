package aegis.server.domain.pointshop.dto.internal;

import java.time.LocalDateTime;

import aegis.server.domain.pointshop.domain.PointShopDrawHistory;
import aegis.server.domain.pointshop.domain.PointShopItem;

public record PointShopDrawInfo(
        Long drawHistoryId, Long memberId, PointShopItem item, Long transactionId, LocalDateTime createdAt) {

    public static PointShopDrawInfo from(PointShopDrawHistory history) {
        return new PointShopDrawInfo(
                history.getId(),
                history.getMember().getId(),
                history.getItem(),
                history.getPointTransaction().getId(),
                history.getCreatedAt());
    }
}
