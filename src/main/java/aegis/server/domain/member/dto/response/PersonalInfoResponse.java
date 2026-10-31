package aegis.server.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import aegis.server.domain.member.domain.*;

public record PersonalInfoResponse(
        String name,
        @Schema(nullable = true) String phoneNumber,
        @Schema(nullable = true) String studentId,
        @Schema(nullable = true) Department department,
        @Schema(nullable = true) Grade grade,
        @Schema(nullable = true) String birthDate,
        @Schema(nullable = true) Gender gender,
        @Schema(nullable = true) ProfileIcon profileIcon,
        Role role) {
    public static PersonalInfoResponse from(Member member) {
        return new PersonalInfoResponse(
                member.getName(),
                member.getPhoneNumber(),
                member.getStudentId(),
                member.getDepartment(),
                member.getGrade(),
                member.getBirthdate(),
                member.getGender(),
                member.getProfileIcon(),
                member.getRole());
    }
}
