package aegis.server.domain.member.domain;

import aegis.server.domain.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birthdate;

    private String phoneNumber;

    public static Member createMember(String oidcId, String email, String name) {
        return Member.builder()
                .role(Role.USER)
                .oidcId(oidcId)
                .email(email)
                .name(name)
                .build();
    }

    public void updateMember(
            Gender gender,
            String birthdate,
            String phoneNumber
    ) {
        this.gender = gender;
        this.birthdate = birthdate;
        this.phoneNumber = phoneNumber;
    }
}
