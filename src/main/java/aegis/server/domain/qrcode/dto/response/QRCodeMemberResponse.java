package aegis.server.domain.qrcode.dto.response;

import aegis.server.domain.member.domain.Member;

public record QRCodeMemberResponse(Long memberId, String name, String studentId) {
    public static QRCodeMemberResponse from(Member member) {
        return new QRCodeMemberResponse(member.getId(), member.getName(), member.getStudentId());
    }
}
