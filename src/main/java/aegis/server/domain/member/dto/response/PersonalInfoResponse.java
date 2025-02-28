package aegis.server.domain.member.dto.response;

import aegis.server.domain.member.domain.*;

public record PersonalInfoResponse(
        String name,
        String birthDate,
        Gender gender,
        String phoneNumber,
        String studentId,
        Department department,
        AcademicStatus academicStatus,
        Grade grade,
        Semester semester,
        Boolean fresh
) {
    public static PersonalInfoResponse from(Member member, Student student) {
        return new PersonalInfoResponse(
                member.getName(),
                member.getBirthdate(),
                member.getGender(),
                member.getPhoneNumber(),
                student.getStudentId(),
                student.getDepartment(),
                student.getAcademicStatus(),
                student.getGrade(),
                student.getSemester(),
                student.getFresh()
        );
    }
}
