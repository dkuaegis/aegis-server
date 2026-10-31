package aegis.server.domain.point.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record PointRankingListResponse(
        long memberCount,
        List<PointRankingResponse> top10,
        @Schema(nullable = true) PointRankingResponse me) {

    public static PointRankingListResponse of(
            long memberCount, List<PointRankingResponse> top10Rankings, PointRankingResponse myRanking) {
        return new PointRankingListResponse(memberCount, top10Rankings, myRanking);
    }
}
