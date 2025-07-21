package aegis.server.domain.point.dto.response;

import java.util.List;

public record PointRankingListResponse(long memberCount, List<PointRankingResponse> top10, PointRankingResponse me) {

    public static PointRankingListResponse of(
            long memberCount, List<PointRankingResponse> top10Rankings, PointRankingResponse myRanking) {
        return new PointRankingListResponse(memberCount, top10Rankings, myRanking);
    }
}
