package aegis.server.domain.member.dto.response;

import aegis.server.domain.member.domain.*;

public record PersonalInfoResponse(
        String name,
        String phoneNumber,
        String studentId,
        Department department,
        Grade grade,
        String birthDate,
        Gender gender,
        ProfileIcon profileIcon) {
    public static PersonalInfoResponse from(Member member) {
        return new PersonalInfoResponse(
                member.getName(),
                member.getPhoneNumber(),
                member.getStudentId(),
                member.getDepartment(),
                member.getGrade(),
                member.getBirthdate(),
                member.getGender(),
                member.getProfileIcon());
    }
}
