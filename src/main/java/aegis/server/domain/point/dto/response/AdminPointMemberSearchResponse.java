package aegis.server.domain.point.dto.response;

import aegis.server.domain.member.domain.Member;

public record AdminPointMemberSearchResponse(Long memberId, String studentId, String memberName) {

    public static AdminPointMemberSearchResponse from(Member member) {
        return new AdminPointMemberSearchResponse(member.getId(), member.getStudentId(), member.getName());
    }
}
