package aegis.server.domain.member.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String oidcId;

    private String discordId;

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

    @Enumerated(EnumType.STRING)
    private JoinProgress joinProgress;

    public static Member createGuestMember(String oidcId, String email, String name) {
        return Member.builder()
                .oidcId(oidcId)
                .email(email)
                .name(name)
                .role(Role.GUEST)
                .joinProgress(JoinProgress.GOOGLE_LOGIN)
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

    public void updateJoinProgress(JoinProgress joinProgress) {
        this.joinProgress = joinProgress;
    }

    public void updateDiscordId(String discordId) {
        this.discordId = discordId;
    }

}
