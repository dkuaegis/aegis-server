package aegis.server.domain.member.dto.response;

public record MemberRecordBackfillResponse(long totalCompletedPayments, long createdRecords, long skippedRecords) {

    public static MemberRecordBackfillResponse of(
            long totalCompletedPayments, long createdRecords, long skippedRecords) {
        return new MemberRecordBackfillResponse(totalCompletedPayments, createdRecords, skippedRecords);
    }
}
