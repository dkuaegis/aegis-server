package aegis.server.domain.googlesheets.dto;

import java.time.LocalDateTime;
import java.util.List;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.pointshop.dto.internal.PointShopDrawInfo;

public record PointShopDrawData(
        LocalDateTime drawDateTime, Long drawHistoryId, Long memberId, String name, String phoneNumber, String item) {

    public static PointShopDrawData from(PointShopDrawInfo info, Member member) {
        return new PointShopDrawData(
                info.createdAt(),
                info.drawHistoryId(),
                info.memberId(),
                member.getName(),
                member.getPhoneNumber(),
                info.item().name());
    }

    public List<Object> toRowData() {
        String formattedDateTime = drawDateTime != null ? drawDateTime.toString() : "";
        return List.of(
                formattedDateTime,
                drawHistoryId,
                memberId,
                name,
                phoneNumber,
                item
                );
    }
}
