package aegis.server.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.member.domain.Member;

public record InstructorStudyMemberResponse(
        String name,
        @Schema(nullable = true) String studentId,
        @Schema(nullable = true) String phoneNumber) {
    public static InstructorStudyMemberResponse from(Member member) {
        return new InstructorStudyMemberResponse(member.getName(), member.getStudentId(), member.getPhoneNumber());
    }
}
