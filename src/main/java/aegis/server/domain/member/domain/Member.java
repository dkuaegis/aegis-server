package aegis.server.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String name;

    private String birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String studentId;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private AcademicStatus academicStatus;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member createGuestMember(String email, String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .role(Role.GUEST)
                .build();
    }

    public void updateMember(
            String birthDate,
            Gender gender,
            String studentId,
            String phoneNumber,
            Department department,
            AcademicStatus academicStatus,
            Grade grade,
            Semester semester
    ) {
        this.birthDate = birthDate;
        this.gender = gender;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.academicStatus = academicStatus;
        this.grade = grade;
        this.semester = semester;
    }

}
