package aegis.server.domain.point.dto.response;

import java.util.List;

public record AdminPointBatchGrantResultResponse(
        int totalRequested,
        int successCount,
        int duplicateCount,
        int failureCount,
        List<AdminPointBatchGrantMemberResultResponse> results) {

    public static AdminPointBatchGrantResultResponse of(List<AdminPointBatchGrantMemberResultResponse> results) {
        int successCount = (int) results.stream()
                .filter(result -> result.status() == AdminPointBatchGrantStatus.SUCCESS)
                .count();
        int duplicateCount = (int) results.stream()
                .filter(result -> result.status() == AdminPointBatchGrantStatus.DUPLICATE)
                .count();
        int failureCount = (int) results.stream()
                .filter(result -> result.status() == AdminPointBatchGrantStatus.FAILED)
                .count();

        return new AdminPointBatchGrantResultResponse(
                results.size(), successCount, duplicateCount, failureCount, results);
    }
}
