package aegis.server.domain.member.dto.response;

import aegis.server.domain.member.domain.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {

    private Long id;

    private String email;

    private String name;

    private String birthDate;

    private Gender gender;

    private String studentId;

    private String phoneNumber;

    private Department department;

    private AcademicStatus academicStatus;

    private Grade grade;

    private Semester semester;

    private JoinProgress joinProgress;

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getBirthDate(),
                member.getGender(),
                member.getStudentId(),
                member.getPhoneNumber(),
                member.getDepartment(),
                member.getAcademicStatus(),
                member.getGrade(),
                member.getSemester(),
                member.getJoinProgress()
        );
    }

}
