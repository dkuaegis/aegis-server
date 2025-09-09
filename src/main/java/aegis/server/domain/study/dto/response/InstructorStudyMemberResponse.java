package aegis.server.domain.study.dto.response;

import aegis.server.domain.member.domain.Member;

public record InstructorStudyMemberResponse(String name, String studentId, String phoneNumber) {
    public static InstructorStudyMemberResponse from(Member member) {
        return new InstructorStudyMemberResponse(member.getName(), member.getStudentId(), member.getPhoneNumber());
    }
}
