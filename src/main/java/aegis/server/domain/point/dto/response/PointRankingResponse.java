package aegis.server.domain.point.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.member.domain.ProfileIcon;

public record PointRankingResponse(
        Long rank,
        String name,
        BigDecimal totalEarnedPoints,
        @Schema(nullable = true) ProfileIcon profileIcon) {

    public static PointRankingResponse of(
            Long rank, String name, BigDecimal totalEarnedPoints, ProfileIcon profileIcon) {
        return new PointRankingResponse(rank, name, totalEarnedPoints, profileIcon);
    }
}
