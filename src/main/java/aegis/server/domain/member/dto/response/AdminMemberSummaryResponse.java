package aegis.server.domain.member.dto.response;

import aegis.server.domain.member.domain.Member;
import aegis.server.domain.member.domain.Role;

public record AdminMemberSummaryResponse(Long memberId, String studentId, String name, String email, Role role) {
    public static AdminMemberSummaryResponse from(Member member) {
        return new AdminMemberSummaryResponse(
                member.getId(), member.getStudentId(), member.getName(), member.getEmail(), member.getRole());
    }
}
