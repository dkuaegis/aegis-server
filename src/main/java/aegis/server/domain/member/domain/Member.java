package aegis.server.domain.member.domain;

import jakarta.persistence.*;

import lombok.*;

import aegis.server.domain.common.domain.BaseEntity;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String oidcId;

    @Column(unique = true)
    private String discordId;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String phoneNumber;

    private String studentId;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private String birthdate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    public static Member create(String oidcId, String email, String name) {
        return Member.builder()
                .role(Role.USER)
                .oidcId(oidcId)
                .email(email)
                .name(name)
                .build();
    }

    public void updatePersonalInfo(
            String phoneNumber, String studentId, Department department, Grade grade, String birthdate, Gender gender) {
        this.phoneNumber = phoneNumber;
        this.studentId = studentId;
        this.department = department;
        this.grade = grade;
        this.birthdate = birthdate;
        this.gender = gender;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public boolean isGuest() {
        return Role.GUEST.equals(this.role);
    }

    public void promoteToUser() {
        this.role = Role.USER;
    }

    public void demoteToGuest() {
        this.role = Role.GUEST;
    }
}
