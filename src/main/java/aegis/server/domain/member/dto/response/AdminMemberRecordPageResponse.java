package aegis.server.domain.member.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import aegis.server.domain.member.domain.MemberRecord;

public record AdminMemberRecordPageResponse(
        List<AdminMemberRecordItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static AdminMemberRecordPageResponse from(Page<MemberRecord> memberRecordPage) {
        return new AdminMemberRecordPageResponse(
                memberRecordPage.getContent().stream()
                        .map(AdminMemberRecordItemResponse::from)
                        .toList(),
                memberRecordPage.getNumber(),
                memberRecordPage.getSize(),
                memberRecordPage.getTotalElements(),
                memberRecordPage.getTotalPages(),
                memberRecordPage.hasNext());
    }
}
